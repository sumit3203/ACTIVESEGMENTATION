package activeSegmentation.filter;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

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
import fftscale.filter.FFTAbstractKernel;
import fftscale.filter.FFTKernelGauss;

import static activeSegmentation.FilterType.SEGM;
import static fftscale.FFTConvolver.*;
import ij.*;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ijaux.datatype.*;
import ijaux.scale.SUtils;

/**
 * @version 	1.0 24 Aug 2019
 * 				
 *   
 * 
 * @author Dimiter Prodanov
 * 		  IMEC
 *
 *
 * @contents
 * This pluign convolves an image with a Gaussian filter
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

@AFilter(key="FGAUSS", value="FFT Gaussian", type=SEGM)
public class FFTGauss_Filter_  implements PlugInFilter, IFilter, IFilterViz {
	private final static String KSZ = "KSZ", GEV="GEV1";
	private final int flags=DOES_ALL + NO_CHANGES + NO_UNDO;
	private ImagePlus imp=null;
	private boolean isEnabled=true;

	private final static String version = "1.1";
	private static double sigma=Prefs.getInt(KSZ,3);
	private static boolean even=Prefs.getBoolean(GEV,false);
	private static boolean showkernel=true;
	private String LEN="G_len",MAX_LEN="G_MAX";
	private  int sz= Prefs.getInt(LEN, 1);
	private  int max_sz= Prefs.getInt(MAX_LEN, 9);
 
	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "FGAUSS";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "FFT Gaussian";

 
	private Map< String, String > settings= new HashMap<String, String>();

	private ImageStack imageStack;
 
	
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
	
	
	void showAbout() {
		IJ.showMessage("FFT Kernel " + version, "The plugin applies a Gaussian kernel to the image");
	}
	
	
	@Override
	public void run(ImageProcessor ip) {
			
		final int[] frame=FFTConvolver.framesize(ip,true);
		int kw=frame[2];
		int kh=frame[3];
		FFTKernelGauss fgauss=new FFTKernelGauss (kw, kh,  sigma, even);
		FFTConvolver proc = new FFTConvolver(ip, fgauss, true);

		FloatProcessor output=proc.convolve();
	
		imp=new ImagePlus("Gauss sigma="+sigma,output);
		imp.show();
		if (showkernel) {
			ComplexFProcessor ckern= new ComplexFProcessor(kw,kh, fgauss.getKernelComplexF());
			ckern.ifftshift();
			final String what=even?"even":"odd";
			new ImagePlus(what+" kernel sigma "+sigma,ckern.stackviz()).show();
		}
	}

	/*
	 * shows dialog
	 */
	public boolean showDialog() {
		GenericDialog gd=new GenericDialog("FFT Gauss Kernel " + version);
		gd.addNumericField("sigma", sigma, 1);
		final String what=even?"even":"odd";
		String[] items=new String[] {"even", "odd"};
		gd.addChoice("even", items,  what);
		gd.addCheckbox("show kernel", false);
		gd.showDialog();
		sigma = gd.getNextNumber();
		even = (gd.getNextChoiceIndex()==0);
		showkernel=gd.getNextBoolean();
		
		if (gd.wasCanceled()) return false;
		return true;
	}
	
	public static void savePreferences(Properties prefs) {
		prefs.put(KSZ, Double.toString(sigma));
		prefs.put(GEV, Boolean.toString(even));
		
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
		FFTKernelGauss fgauss=new FFTKernelGauss (kw,kh,  3.0, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss, true);
		IComplexFArray kern=fgauss.getKernelComplexF();
		
		
		FloatProcessor output=proc.convolve();
		new ImagePlus("convovled",output).show();
		 
		 
		ComplexFProcessor ckern=new ComplexFProcessor(kw,kh, kern);
		ckern.ifftshift();
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
		// TODO Auto-generated method stub
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		return true;
	}


	@Override
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList) {
		// TODO Auto-generated method stub
		for (int sigma=sz; sigma<= max_sz; sigma +=2){		
			ImageProcessor fp=filter(image, sigma);
			String imageName=path+"/"+FILTER_KEY+"_"+sigma+".tif" ;
			IJ.save(new ImagePlus(FILTER_KEY+"_" + sigma, fp),imageName );
		}
	}
	
	private FloatProcessor filter(ImageProcessor ip, double sigma) {
		
		int width=ip.getWidth();
		int height=ip.getHeight();
		int[] frame=framesize(new int[]{width,height},true);
		int kw=frame[2];
		int kh=frame[3];
		FFTKernelGauss fgauss=new FFTKernelGauss (kw,kh,  sigma, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss, true);
		IComplexFArray kern=fgauss.getKernelComplexF();
		
		
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

	/*
	 * by convention we will plot the lowest order derivative in 1D
	 */
	private double gauss(double x){
		return  exp(-x*x/2.0) / (2.0*sqrt(PI));
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
			data[1][i]=gauss(data[0][i]);
		}
		return data;
	}
	

}
