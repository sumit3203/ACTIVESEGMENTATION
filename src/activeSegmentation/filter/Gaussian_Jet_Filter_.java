package activeSegmentation.filter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.gui.DialogListener;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.*;

import static activeSegmentation.FilterType.SEGM;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import activeSegmentation.AFilter;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import dsp.Conv;

/**
 * @version   	 28 Feb 2019
 * 					- gaussian jet
 *   
 * 
 * @author Dimiter Prodanov,IMEC , Sumit Kumar Vohra
 *
 *
 * @contents
 * This pluign convolves an image with a Gaussian derivative of order  (n,m).
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
@AFilter(key="GAUSSIAN Jet", value="Gaussian Jet", type=SEGM)
public class Gaussian_Jet_Filter_ implements ExtendedPlugInFilter, DialogListener,IFilter, IFilterViz {

    @SuppressWarnings("unused")
    private PlugInFilterRunner pfr=null;

	final int flags=DOES_ALL+CONVERT_TO_FLOAT+SUPPORTS_MASKING+KEEP_PREVIEW;
	private String version="2.0";
	
	@SuppressWarnings("unused")
	private int nPasses=1;
	   
	@SuppressWarnings("unused")
	private int pass=0;
	public final static String SIGMA="LOG_sigma", LEN="G_len" ,MAX_LEN="G_MAX", 
			ISSEP="G_SEP", GN="G_Xn", GM="G_Yn", SCNORM="G_SCNORM";

	private static int sz = Prefs.getInt(LEN, 2);
	private  int max_sz= Prefs.getInt(MAX_LEN, 8);
	private float[][] kernel=null;
	public static boolean debug=IJ.debugMode;

	private static int nn = Prefs.getInt(GN, 1);
	private static int mm = Prefs.getInt(GM, 0);

	private static boolean sep=Prefs.getBoolean(ISSEP, false);
	private static boolean scnorm=Prefs.getBoolean(SCNORM, false);
	private ImagePlus image=null;	
	private boolean isFloat=false;
	private boolean isRGB=false;
	private static int wnd=3;
	private boolean isEnabled=true;
	private int position_id=-1;


	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "GAUSSIAN Jet";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "Gaussian Jet";

	
	
	private Map< String, String > settings= new HashMap<String, String>();

	private ImageStack imageStack;

	/*
	 * @param args - args[0] should point to the folder where the plugins are installed 
	 */
	public static void main(String[] args) {

		try {

			File f=new File(args[0]);

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
		}

	}

	public void initialseimageStack(ImageStack img){
		this.imageStack = img;
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		image=imp;
		isFloat= (imp.getType()==ImagePlus.GRAY32);
		isRGB = (imp.getType()==ImagePlus.COLOR_RGB);
		cal=image.getCalibration();

		return  flags;
	}

	private boolean doCalib = false;
	
	/*
	 * This variable is to calibrate the Image Window
	 */
	private Calibration cal=null;

	@Override
	public void run(ImageProcessor ip) {
		int r = (sz-1)/2;
		if (wnd<0)
			wnd=-wnd;
		if (wnd>5)
			wnd=5;

		GScaleSpace sp=new GScaleSpace(r, wnd);
		
		imageStack=new ImageStack(ip.getWidth(),ip.getHeight());
		ImageStack fpaux=filter(ip, sp,  scnorm,nn);
		image=new ImagePlus("Convolved_"+nn+"_"+mm, fpaux);
		//image.updateAndDraw();
		image.show();	
	}




	/**
	 * Apply filter to input image (in place)
	 * @param inputImage input image
	 * @param size kernel size (it must be odd)
	 * @param nAngles number of angles
	 * @return false if error
	 */
	public Pair<Integer,ImageStack> applyFilter(ImageProcessor ip){
		int index = position_id;
		ImageStack imageStack=new ImageStack(ip.getWidth(),ip.getHeight());
		for (int sigma=sz; sigma<= max_sz; sigma *=2){		
			GScaleSpace sp=new GScaleSpace(sigma);
			ImageStack fs=filter(ip.duplicate(), sp, scnorm,nn);
			//IJ.save(new ImagePlus(FILTER_KEY+"_" + sigma, fp), PATH+FILTER_KEY+"_"+index+"_"+sigma+Common.TIFFORMAT );
			imageStack.addSlice( FILTER_KEY+"_" + sigma, fs);		
		}
		initialseimageStack(imageStack);
		return new Pair<Integer,ImageStack>(index, imageStack);
	}

	@Override
	public void applyFilter(ImageProcessor image, String filterPath,List<Roi> roiList) {

			for (int sigma=sz; sigma<= max_sz; sigma *=2){		
				GScaleSpace sp=new GScaleSpace(sigma);
				ImageStack is=filter(image,  sp, scnorm,nn);			
				String imageName=filterPath+"/"+FILTER_KEY+"_"+sigma+".tif" ;
				IJ.save(new ImagePlus(FILTER_KEY+"_" + sigma, is),imageName );
			}

	}
	
	private ImageStack filter(ImageProcessor ip, GScaleSpace sp, final boolean scnorm, int n){

		ImageProcessor ipaux=ip.duplicate();
		if (!isFloat && !isRGB) 
			ipaux=ipaux.toFloat(0, null);
		pass++;

		/*
		 * define an array of derivatives
		 * then convolve with each combination
		 *  d_x(k) d_y(n-k), i<=j
		 * */
		double sigma=sp.getSigma();
		float[] kernx=null;
		if (n==0) {
			kernx=sp.gauss1D();
		} else {
			kernx=sp.diffNGauss1D(n);
			if (scnorm) {
				scnorm(kernx,sigma,n);
			}
		}
		GScaleSpace.flip(kernx);	
 
		kernel=new float[n][];
		kernel[0]=kernx;
		for (int i=1;i<n; i++) {
			if (scnorm) {
				float[] kerny=sp.diffNGauss1D(i);
				GScaleSpace.flip(kerny);
				scnorm(kerny,sigma,n);
				kernel[i]=kerny;
			} else {
				float[] kerny=sp.diffNGauss1D(i);
				GScaleSpace.flip(kerny);
				kernel[i]=kerny;
			}
		}
		
		ImageStack is=new ImageStack(ip.getWidth(), ip.getHeight());
		
		long time=-System.nanoTime();	
		Conv cnv=new Conv();
		
		for (int i=0; i <n; i++) { 
			FloatProcessor fpaux= (FloatProcessor) ipaux.duplicate();
			for (int j=0; j <n; j++) {
				cnv.convolveSep(fpaux, kernel[i], kernel[n-i]);
				is.addSlice("dx [" +i+"] dy["+j+"]", fpaux);
			}

		}
		time+=System.nanoTime();
		time/=1000.0f;		
		System.out.println("elapsed time: " + time +" us");

		return is;

	}

	private void scnorm(float[] kern, double sigma, int n) {
		sigma=Math.pow(sigma, n);
		for (int i=0; i<kern.length; i++) {
			kern[i]*=sigma;
		}
	}

	/* Saves the current settings of the plugin for further use
	 * 
	 *
	 * @param prefs
	 */
	public static void savePreferences(Properties prefs) {
		prefs.put(GN, Integer.toString(nn));
		prefs.put(GM, Integer.toString(mm));
		prefs.put(LEN, Integer.toString(sz));
		prefs.put(ISSEP, Boolean.toString(sep));
		prefs.put(SCNORM, Boolean.toString(scnorm));
		// prefs.put(SIGMA, Float.toString(sigma));

	}


	public float[] getKernel(int i) {
		return kernel[i];
	}

	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		int r = (sz-1)/2;
		GenericDialog gd=new GenericDialog("Gauss. derivative " + version);
		gd.addNumericField("span x sigma", wnd, 3);
		gd.addNumericField("half width", r, 1);
		gd.addNumericField("order in x", nn, 1);
		gd.addNumericField("order in y", mm, 1);
		gd.addCheckbox("Show kernel", debug);
		gd.addCheckbox("Separable", sep);
		gd.addCheckbox("Scale nomalize", scnorm);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		
		if (cal!=null) {
			if (!cal.getUnit().equals("pixel"))
				gd.addCheckbox("units ( "+cal.getUnit() + " )", doCalib); 
		}	
		
		if (gd.wasCanceled())
			return DONE;


		return IJ.setupDialog(imp, flags);
	}

	// Called after modifications to the dialog. Returns true if valid input.
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		wnd = (int)(gd.getNextNumber());
		int r = (int)(gd.getNextNumber());
		nn = (int)(gd.getNextNumber());
		mm = (int)(gd.getNextNumber());
		debug = gd.getNextBoolean();
		sep = gd.getNextBoolean();
		scnorm = gd.getNextBoolean();
		
		if (cal!=null)
			doCalib=gd.getNextBoolean();
		
		sz = 2*r+1;
		if (gd.wasCanceled())
			return false;

		return r>0;
	}


	@Override
	public void setNPasses (int nPasses) {
		this.nPasses = nPasses;
	}

	@Override
	public boolean reset() {
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		sep= Prefs.getBoolean(ISSEP, true);
		scnorm=Prefs.getBoolean(SCNORM, false);
		nn = Prefs.getInt(GN, 1);
		mm = Prefs.getInt(GM, 0);
		return true;
	}

	@Override
	public Map<String, String> getDefaultSettings() {

		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		settings.put(ISSEP, Boolean.toString(sep));
		settings.put(SCNORM, Boolean.toString(scnorm));
		settings.put(GN, Integer.toString(nn));
		settings.put(GM, Integer.toString(mm));

		return this.settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		sep=Boolean.parseBoolean(settingsMap.get(ISSEP));
		scnorm=Boolean.parseBoolean(settingsMap.get(SCNORM));
		nn=Integer.parseInt(settingsMap.get(GN));
		mm=Integer.parseInt(settingsMap.get(GM));

		return true;
	}


	@Override
	public String getKey() {
		return FILTER_KEY;
	}

	@Override
	public String getName() {
		return FILTER_NAME;
	}


	/*
	 * by convention we will plot the lowest order derivative in 1D
	 */
	private double gdKernel(double x){
		return -x*exp(-x*x/2.0) / (2.0*sqrt(PI));
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
	public double[][] kernelData() {
		final int n=40;
		double [][] data=new double[2][n];
		data[0]=SUtils.linspace(-10.0, 10.0, n);
		for(int i=0; i<n; i++){
			data[1][i]=gdKernel(data[0][i]);
		}
		return data;
	}

}
