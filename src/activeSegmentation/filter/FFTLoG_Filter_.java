package activeSegmentation.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import fftscale.*;
import fftscale.filter.FFTKernelLoG;

import static activeSegmentation.FilterType.SEGM;
import static fftscale.FFTConvolver.*;
import ij.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ijaux.datatype.*;
import ijaux.scale.SUtils;

import static java.lang.Math.*;

/**
 * @version 	
 * 				1.3.2 13 Feb 2023
 * 				- help annotations
 * 				1.3.1 7 Oct 2021
 * 				- annotations
 * 				1.3 28 Jun 2021
 * 				- all parameters revealed to Active Segmentation
 * 				1.2 31 Oct 2019
 * 				- Active Segmentation version
 * 				1.1 10 Sept 2019
 * 				- bug fixes
 * 				1.0 24 Aug 2019
 * 				
 *   
 * 
 * @author Dimiter Prodanov
 * 		   IMEC, BAS
 *
 *
 * @contents
 * This pluign convolves an image with a power of the alpha Laplacian of Gaussian filter.
 * This filter implements alpha scale space.
 * 
 * 
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

@AFilter(key="FLOG", value="FFT Laplacian of Gaussian", type=SEGM, help = "/help.html")
public class FFTLoG_Filter_  implements PlugInFilter, IFilter, IFilterViz {
	
	public final static String KSZ = "KSZ", GEV="GEV2", ORD="ORDL",LEN="G_len",MAX_LEN="G_MAX";
	private final int flags=DOES_ALL + NO_CHANGES + NO_UNDO;
	private ImagePlus imp;

	private final static String version = "1.1";
	
	public static double sigma=Prefs.getInt(KSZ,3);
	
	private static boolean showkernel=true;
		
	////////////////////
	// Annotated fields
	///////////////////
	
	@AFilterField(key=ORD, value="order")
	public static double order=Prefs.getDouble(ORD,1.0);
	
	@AFilterField(key=GEV, value="even")
	public static boolean even=Prefs.getBoolean(GEV, false);
 
	@AFilterField(key=LEN, value="initial scale")
	public int sz= Prefs.getInt(LEN, 1);
	
	@AFilterField(key=MAX_LEN, value="max scale")
	public  int max_sz= Prefs.getInt(MAX_LEN, 9);


	
	/* NEW VARIABLES*/

	private Map< String, String > settings= new HashMap<>();

	private boolean isEnabled=true;

	
	void showAbout() {
		IJ.showMessage("FFT LoG " + version, "The plugin applies a Fractional LoG kernel to the image");
	}
	
	@Override
	public String helpInfo() {
		/*return "s=sigma^2 - scale; \r\n "
				+ "normalized - scale by sigma; \r\n "
				+ "separable - keep enabled for faster run;";*/
		return getHelpResource();
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		if (imp==null) return DONE;
		if (IJ.versionLessThan("1.49")) return DONE;

		if (arg.equals("about")){
			showAbout();
			return DONE;
		}

		int ret = showDialog() ? flags : DONE;

		return ret;
	}


	@Override
	public void run(ImageProcessor ip) {

		final int[] frame=FFTConvolver.framesize(ip,true);
		int kw=frame[2];
		int kh=frame[3];
		FFTKernelLoG fgauss=new FFTKernelLoG (kw, kh, order, sigma,  even);
		imp=convolve(ip, fgauss);
		imp.show();
		if (showkernel) {
			ComplexFProcessor ckern= new ComplexFProcessor(kw,kh, fgauss.getKernelComplexF());
			ckern.ifftshift();
			final String what=even?"even":"odd";
			new ImagePlus(what+" kernel sigma "+sigma+" order "+order,ckern.stackviz()).show();
		}
	}

	/**
	 * @param ip
	 * @param fgauss
	 */
	private ImagePlus convolve(ImageProcessor ip, FFTKernelLoG fgauss) {
		FFTConvolver proc = new FFTConvolver(ip, fgauss, true);
		FloatProcessor output=proc.convolve();
		ImagePlus imp=new ImagePlus("LoG sigma="+sigma+" order "+order,output);
		return imp;
	}


	public boolean showDialog() {
		GenericDialog gd=new GenericDialog("FFT LoG Kernel " + version);
		gd.addNumericField("sigma", sigma, 1);
		gd.addNumericField("order", order, 1);
		final String what=even?"even":"odd";
		String[] items=new String[] {"even", "odd"};
		gd.addChoice("even", items,  what);

		gd.addCheckbox("show kernel", false);
		gd.showDialog();

		if (gd.wasCanceled()) return false;

		sigma = gd.getNextNumber(); 	
		order = gd.getNextNumber();
		even = (gd.getNextChoiceIndex()==0);
		showkernel=gd.getNextBoolean();

		return true;
	}

	public void savePreferences(Properties prefs) {
		prefs.put(KSZ, Double.toString(sigma));
		prefs.put(LEN, Double.toString(sz));
		prefs.put(MAX_LEN, Double.toString(max_sz));
		prefs.put(ORD, Double.toString(order));
		prefs.put(GEV, Boolean.toString(even));
	}

	/*
	 * @param args - args[0] should point to the folder where the plugins are installed 
	 */
	public static void main(String[] args) {

		new ImageJ();
		IJ.run("Blobs (25K)");
		ImageProcessor ip=IJ.getImage().getProcessor();
		int width=ip.getWidth();
		int height=ip.getHeight();
		int[] frame=framesize(new int[]{width,height},true);
		int kw=frame[2];
		int kh=frame[3];
		System.out.println(kw);
		System.out.println(kh);
		FFTKernelLoG fgauss=new FFTKernelLoG (kw,kh, 1, sigma, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss);
		IComplexFArray kern=fgauss.getKernelComplexF();
		ComplexFProcessor ckern=new ComplexFProcessor(kw,kh, kern);

		FloatProcessor output=proc.convolve();


		new ImagePlus("convovled",output).show();
		new ImagePlus("kernel",ckern.stackviz()).show();
		
		FFTLoG_Filter_ filter=new FFTLoG_Filter_();
		System.out.println("annotated fields");
		System.out.println(filter.getAnotatedFileds());
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		settings.put(ORD, Double.toString(order));
		settings.put(GEV, Boolean.toString(even));
		return settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		order=Double.parseDouble(settingsMap.get(ORD));
		even=Boolean.parseBoolean(settingsMap.get(GEV));
		return true;
	}


	@Override
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList) {
		String key=getKey();
		// re-prarametrization by sz
		final int sord=(int) (order*10);
		for (int sigma=sz; sigma<= max_sz; sigma *=2){		
			ImageProcessor fp=filter(image, order, sigma);
			String imageName=path+"/"+key+"_"+sord+"_"+sigma+".tif" ;
			IJ.save(new ImagePlus(key+"_"+sord+"_" + sigma, fp),imageName );
		}
	}

	// TODO eventually to leave only 1 method - order!
	public FloatProcessor filter(ImageProcessor ip, double ord, double sigma) {

		int width=ip.getWidth();
		int height=ip.getHeight();
		int[] frame=framesize(new int[]{width,height},true);
		int kw=frame[2];
		int kh=frame[3];
		FFTKernelLoG fgauss=new FFTKernelLoG (kw,kh, ord, sigma, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss);

		FloatProcessor output=proc.convolve();
		return output;

	}
	

	
	private double logKernel(double x){
		final double x2=x*x;
		return (x2-2)* exp(-0.5*x2)/(2.0*sqrt(PI));
	}


	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.isEnabled= isEnabled;
	}


	@Override
	public boolean reset() {
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		return true;
	}

	@Override
	public double[][] kernelData() {
		final int n=40;
		double [][] data=new double[2][n];
		data[0]=SUtils.linspace(-10.0, 10.0, n);
		for(int i=0; i<n; i++){
			data[1][i]=logKernel(data[0][i]);
		}
		return data;
	}



}
