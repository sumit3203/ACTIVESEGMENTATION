package activeSegmentation.filter;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.DialogListener;
import ij.measure.Calibration;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.*;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import dsp.Conv;

import static activeSegmentation.FilterType.SEGM;
import static java.lang.Math.*;

/**
 * @version 	
 * 				1.2 13 Feb 2023
 * 				- help annotations
 * 				1.1 19 Nov 2017 
 * 					- IFilter added
 * 					- refactoring
 * 				1.0 26 Sept 2017
 * 				
 *   
 * 
 * @author Dimiter Prodanov
 * 		  
 *
 *
 * @contents
 * The plugin performs curvature filtering
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



@AFilter(key="CURVATURE", value="Curvature 3D", type=SEGM, help = "/help.html")
public class GaussK_Filter_ implements ExtendedPlugInFilter, DialogListener, IFilter, IFilterViz {

	private PlugInFilterRunner pfr=null;

	final int flags=DOES_ALL+KEEP_PREVIEW+ NO_CHANGES;
	private String version="1.0";
	private int nPasses=1;
	private int pass;

	public final static String SIGMA="GK_sigma", MAX_LEN="G_MAX", LEN="GK_len";

	@AFilterField(key=LEN, value="initial scale")
	private static int sz= Prefs.getInt(LEN, 2);
	//private static float sigma=(float) Prefs.getDouble(SIGMA, 2.0f);
	
	@AFilterField(key=MAX_LEN, value="max scale")
	private  int max_sz= Prefs.getInt(MAX_LEN, 8);

	private float[][] kernel=null;

	private ImagePlus image=null;
	public static boolean debug=true;//IJ.debugMode;

	public  boolean fulloutput=false;

	private boolean isFloat=false;


	final int Ox=0, Oy=1, Oz=2;

	private boolean doCalib = false;
	private Calibration cal=null;
	
	
	/* NEW VARIABLES*/

	private boolean isEnabled=true;


	/** It stores the settings of the Filter. */
	private Map< String, String > settings= new HashMap<>();


	/**
	 * 
	 */
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		image=imp;
		isFloat= (image.getType()==ImagePlus.GRAY32);
		//hasRoi=imp.getRoi()!=null;
		cal=image.getCalibration();
		return  flags;
	}


	/*
	 * (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		ip.snapshot();

		if (!isFloat) 
			ip=ip.toFloat(0, null);

		pass++;
		int r = (sz-1)/2;
		GScaleSpace sp=new GScaleSpace(r);
		//GScaleSpace sp=new GScaleSpace(sigma);

		ImageStack imageStack=new ImageStack(ip.getWidth(),ip.getHeight());

		imageStack = filter(ip,sp,imageStack);


		String stackloc="";

		if (image.getStackSize()> 1) {
			stackloc=" z= "+image.getCurrentSlice();
			//System.out.println("stack location "+stackloc);
		}

		int apos=2;
		if (fulloutput) { 
			apos=5;
		}

		image=new ImagePlus("Gauss K result hw="+r+stackloc,imageStack);
		image.show();
		image.setPosition(apos);
		image.getProcessor().resetMinAndMax();	
	}




	/**
	 * @param i
	 * @return
	 */
	public float[] getKernel(int i) {
		return kernel[i];
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
	 */
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		int r = (sz-1)/2;
		GenericDialog gd=new GenericDialog("Gaussian Curvature " + version);

		gd.addNumericField("half width", r, 2);
		//gd.addNumericField("sigma", sigma, 1);
		gd.addCheckbox("Show kernel", debug);
		gd.addCheckbox("Full output", fulloutput);	
		if (cal!=null) {
			if (!cal.getUnit().equals("pixel"))
				gd.addCheckbox("units ( "+cal.getUnit() + " )", doCalib); 
		}		

		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.setResizable(false);
		gd.showDialog();

		//pixundo=imp.getProcessor().getPixelsCopy();
		if (gd.wasCanceled()) {			
			return DONE;
		}

		return IJ.setupDialog(imp, flags);
	}




	// Called after modifications to the dialog. Returns true if valid input.
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		double r = (int)(gd.getNextNumber());
		//sigma = (float) (gd.getNextNumber());
		debug = gd.getNextBoolean();
		fulloutput = gd.getNextBoolean();
		if (cal!=null)
			doCalib=gd.getNextBoolean();

		if (doCalib) {
			r= (r/cal.pixelWidth);
		}
		sz =  (2*(int)r+1);
		if (gd.wasCanceled()) {

			return false;
		}
		return r>0;
		//return sigma>0;
	}


	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
	 */
	@Override
	public void setNPasses (int nPasses) {
		this.nPasses = nPasses;
	}

	/* Saves the current settings of the plugin for further use
	 * 
	 *
	 * @param prefs
	 */
	public static void savePreferences(Properties prefs) {
		prefs.put(LEN, Integer.toString(sz));
		// prefs.put(SIGMA, Float.toString(sigma));

	}

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




	@Override
	public Map<String, String> getDefaultSettings() {
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		//settings.put(FULL_OUTPUT, Boolean.toString(fulloutput));

		return this.settings;
	}


	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		//fulloutput= Boolean.parseBoolean(settingsMap.get(FULL_OUTPUT));
		return true;
	}

 


	@Override
	public void applyFilter(ImageProcessor processor, String filterPath,List<Roi> roiList) {
			for (int sigma=sz; sigma<= max_sz; sigma *=2){		
				ImageStack imageStack=new ImageStack(processor.getWidth(),processor.getHeight());
				GScaleSpace sp=new GScaleSpace(sigma);
				imageStack=filter(processor, sp,  imageStack);
				for(int j=1;j<=imageStack.getSize();j++){
					String imageName=filterPath+"/"+imageStack.getSliceLabel(j)+".tif" ;
					IJ.save(new ImagePlus(imageStack.getSliceLabel(j), imageStack.getProcessor(j)),imageName );
				}

			}

	}
	
	
	private ImageStack filter(ImageProcessor ip, GScaleSpace sp,  ImageStack imageStack) {
		if (!isFloat) 
			ip=ip.convertToFloat();

		
		int r=sp.getSize();
		//System.out.println("IN REEAL CODE");
		float[] kernx= sp.gauss1D();
		SUtils.flip(kernx);		
		float[] kern_diff2= sp.diff2Gauss1D();
		SUtils.flip(kern_diff2);

		float[] kern_diff1=sp.diffGauss1D();
		SUtils.flip(kern_diff1);

		kernel=new float[4][];
		kernel[0]=kernx;
		kernel[1]=kern_diff2;
		kernel[2]=kern_diff1;

		float[] kernel2=sp.computeDiff2Kernel2D();
		kernel[3]=kernel2;
		SUtils.flip(kernel2);  // symmetric but this is the correct way

		int sz= sp.getSize();
		if (debug && pass==1) {
			FloatProcessor fpkern2=new FloatProcessor(sz,sz);

			float[][] disp= new float[2][];

			disp[0]=GScaleSpace.joinXY(kernel, 0, 1);
			disp[1]=GScaleSpace.joinXY(kernel, 1, 0);

			for (int i=0; i<sz*sz; i++)
				fpkern2.setf(i, disp[0][i]+ disp[1][i]);

			new ImagePlus("kernel sep",fpkern2).show();


		}
		//System.out.println("Calculating Curvature");
		long time=-System.nanoTime();	
		FloatProcessor fpaux= (FloatProcessor) ip;

		Conv cnv=new Conv();

		FloatProcessor gradx=(FloatProcessor) fpaux.duplicate();
		FloatProcessor grady=(FloatProcessor) fpaux.duplicate();
		FloatProcessor lap_xx=(FloatProcessor) fpaux.duplicate();
		FloatProcessor lap_yy=(FloatProcessor) fpaux.duplicate();
		FloatProcessor lap_xy=(FloatProcessor) fpaux.duplicate();

		cnv.convolveFloat1D(gradx, kern_diff1, Ox);
		cnv.convolveFloat1D(gradx, kernx, Oy);

		cnv.convolveFloat1D(grady, kern_diff1, Oy);
		cnv.convolveFloat1D(grady, kernx, Ox);

		cnv.convolveFloat1D(lap_xx, kern_diff2, Ox);
		cnv.convolveFloat1D(lap_xx, kernx, Oy);

		cnv.convolveFloat1D(lap_yy, kern_diff2, Oy);
		cnv.convolveFloat1D(lap_yy, kernx, Ox);

		cnv.convolveFloat1D(lap_xy, kern_diff1, Oy);
		cnv.convolveFloat1D(lap_xy, kern_diff1, Ox);
		int width=ip.getWidth();
		int height=ip.getHeight();

		FloatProcessor lap_k1k2=new FloatProcessor(width, height); //  Gaussian  curvature component
		FloatProcessor lap_kk=new FloatProcessor(width, height); // mean curvature component


		for (int i=0; i<width*height; i++) {
			// components of the gradient
			double gx=gradx.getf(i);
			double gy=grady.getf(i);

			// components of the Hessian
			double gxy=lap_xy.getf(i);

			double gxx=lap_xx.getf(i);
			double gyy=lap_yy.getf(i);

			
			double dx=gx*gy;
			gx*=gx;
			gy*=gy;		


			double amp=(1.0+ gx+gy); 
			
			//if (abs(amp) < 1.0e8) amp=1e-6;
			
			double damp=amp*amp;

			// Log Gaussian curvature
			//https://en.wikipedia.org/wiki/Gaussian_curvature
			//float gk=(float)(  log( abs(( gxx*gyy - gxy*gxy) * damp) +1e-6)  );
			float gk=(float)( ( gxx*gyy - gxy*gxy) / damp  );

			// mean curvature
			float mk=(float)(0.5*((1+gx)* gyy - 2.0*dx *gxy + (1+gy)*gxx  )/sqrt(amp*damp));

			if (abs(gk) <1e-8) gk=0;
			if (abs(mk) <1e-8) mk=0;	

			lap_k1k2.setf(i, gk);
			lap_kk.setf(i, mk);

		}


		//int apos=2;
		String fkey=this.getKey();
		
		if (fulloutput) {
			imageStack.addSlice(fkey+"_X_diff_"+r, gradx);
			imageStack.addSlice(fkey+"_Y_diff_"+r, grady);
			imageStack.addSlice(fkey+"_XX_diff_"+r, lap_xx);
			imageStack.addSlice(fkey+"_YY_diff_"+r, lap_yy);
			imageStack.addSlice(fkey+"_XY_diff_"+r, lap_xy);
			//apos=5;
		}

		imageStack.addSlice(fkey+"_Gauss_K1xK2_"+r, lap_k1k2);
		lap_kk.resetMinAndMax();
		imageStack.addSlice(fkey+"_Gauss_K1_K2_"+r, lap_kk);

		time+=System.nanoTime();
		time/=1000.0f;
		System.out.println("elapsed time: " + time +" us");

		return imageStack;
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


}