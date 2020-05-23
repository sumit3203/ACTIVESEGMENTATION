package activeSegmentation.filter;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import activeSegmentation.AFilter;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import fftscale.*;
import fftscale.filter.FFTKernelGauss;
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
 * 
 * 				
 * 				1.2 31 Oct 2019
 * 				- Active Segementation version
 * 				1.1 10 Sept 2019
 * 				- bug fixes
 * 				1.0 24 Aug 2019
 * 				
 *   
 * 
 * @author Dimiter Prodanov
 * 		  IMEC
 *
 *
 * @contents
 * This pluign convolves an image with a power of the Laplacian of Gaussian Derivative filter
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

@AFilter(key="FLOG", value="FFT Laplacian of Gaussian", type=SEGM)
public class FFTLoG_Filter_  implements PlugInFilter, IFilter, IFilterViz {
	private final static String KSZ = "KSZ", GEV="GEV2", ORD="ORDL";
	private final int flags=DOES_ALL + NO_CHANGES + NO_UNDO;
	private ImagePlus imp;

	private final static String version = "1.0";
	private static double sigma=Prefs.getInt(KSZ,3);
	private static double order=Prefs.getDouble(ORD,1.0);
	private static boolean showkernel=true;
	private static boolean even=Prefs.getBoolean(GEV,false);
	private String LEN="G_len",MAX_LEN="G_MAX";
	private  int sz= Prefs.getInt(LEN, 1);
	private  int max_sz= Prefs.getInt(MAX_LEN, 9);

	private ImageStack imageStack;
	
	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "FLOG";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "FFT Laplacian of Gaussian";

	private Map< String, String > settings= new HashMap<String, String>();

	private boolean isEnabled=true;

	void showAbout() {
		IJ.showMessage("FFT LoG " + version, "The plugin applies a Gaussian kernel to the image");
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
		imp=filter(ip, fgauss);
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
	private ImagePlus filter(ImageProcessor ip, FFTKernelLoG fgauss) {
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

	public static void savePreferences(Properties prefs) {
		prefs.put(KSZ, Double.toString(sigma));
		prefs.put(ORD, Double.toString(order));
	}

	/*
	 * @param args - args[0] should point to the folder where the plugins are installed 
	 */
	public static void main(String[] args) {

		/*
		try {
			File f = new File(args[0]);
			if (f.exists() && f.isDirectory() ) {
				System.setProperty("plugins.dir", args[0]);
				new ImageJ();
			} else {
				throw new IllegalArgumentException();
			}
		}
		catch (Exception ex) {
			IJ.log("plugins.dir misspecified\n");
			ex.printStackTrace();
		}*/
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
		FFTKernelLoG fgauss=new FFTKernelLoG (kw,kh, 0, sigma, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss);
		IComplexFArray kern=fgauss.getKernelComplexF();
		ComplexFProcessor ckern=new ComplexFProcessor(kw,kh, kern);

		FloatProcessor output=proc.convolve();


		new ImagePlus("convovled",output).show();
		new ImagePlus("kernel",ckern.stackviz()).show();
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		// TODO Auto-generated method stub
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		return settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		return true;
	}


	@Override
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList) {
		for (int sigma=sz; sigma<= max_sz; sigma +=2){		
			ImageProcessor fp=filter(image, sigma);
			String imageName=path+"/"+FILTER_KEY+"_"+sigma+".tif" ;
			IJ.save(new ImagePlus(FILTER_KEY+"_" + sigma, fp),imageName );
		}
	}

	// TODO eventually to leave only 1 method
	public FloatProcessor filter(ImageProcessor ip, double sigma) {

		int width=ip.getWidth();
		int height=ip.getHeight();
		int[] frame=framesize(new int[]{width,height},true);
		int kw=frame[2];
		int kh=frame[3];
		FFTKernelLoG fgauss=new FFTKernelLoG (kw,kh, 0, sigma, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss);

		FloatProcessor output=proc.convolve();
		return output;

	}
	
	@Override
	public String getKey() {
		return this.FILTER_KEY;
	}

	@Override
	public String getName() {
		return this.FILTER_NAME;
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
