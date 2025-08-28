package activeSegmentation.filter;
import dsp.ConvFactory;
import dsp.IConv;
import ij.IJ;
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
import java.util.*;
import java.util.List;

import static activeSegmentation.FilterType.SEGM;
import static java.lang.Math.*;

import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import dsp.cpu.Conv;

/**
 * @version 	1.0 15 Dec 2025
 * 				
 * 				based on Hessian_Filter 1.3 13 Feb 2023
 * 				
 *   
 * 
 * @author Dimiter Prodanov, BAS
 *
 *
 * @contents
 * The plugin computes the eigenvalues of the Weingarten map
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

@AFilter(key="WEING", value="Weingarten components", type=SEGM, help = "/help.html")
public class Ridge_Filter_ implements ExtendedPlugInFilter, DialogListener, IFilter, IFilterViz {
    @SuppressWarnings("unused")

	private PlugInFilterRunner pfr=null;

	final int flags=DOES_ALL+KEEP_PREVIEW+ NO_CHANGES;
	private String version="2.0";
    @SuppressWarnings("unused")

	private int nPasses=1;
	private int pass;

	public final static String SIGMA="HESS_sigma",MAX_LEN="H_MAX",FULL_OUTPUT="HFull_out",LEN="H_len";

	@AFilterField(key=LEN, value="initial scale")
	public  int sz= Prefs.getInt(LEN, 2);
	
	@AFilterField(key=MAX_LEN, value="max scale")
	public  int max_sz= Prefs.getInt(MAX_LEN, 8);
	
	private boolean isEnabled=true;
	private float[][] kernel=null;

	private ImagePlus image=null;
	public static boolean debug=IJ.debugMode;

	@AFilterField(key=FULL_OUTPUT, value="full output")
	public boolean fulloutput=Prefs.getBoolean(FULL_OUTPUT, true);

	private boolean isFloat=false;
 

	/* NEW VARIABLES*/
 	
	/** It stores the settings of the Filter. */
	private Map< String, String > settings= new HashMap<>();	
  

	/**
	 * This method is to setup the PlugInFilter using image stored in ImagePlus 
	 * and arguments of filter
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		image=imp;
		isFloat= (image.getType()==ImagePlus.GRAY32);
  
		cal=image.getCalibration();
		return  flags;
	}

	final int Ox=0, Oy=1, Oz=2;

	// It is used to check whether to calibrate or not
	private boolean doCalib = false;
	
	/*
	 * This variable is to calibrate the Image Window
	 */
	private Calibration cal=null;
	
	
	@Override
	public String helpInfo() {
		/*return "s=sigma^2 - scale; \r\n "
				+ "normalized - scale by sigma; \r\n "
				+ "separable - keep enabled for faster run;";*/
		return getHelpResource();
	}
	
	/*
	 * (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		int r =sz; // (sz-1)/2;
		GScaleSpace sp=new GScaleSpace(r);

		ImageStack imageStack=new ImageStack(ip.getWidth(),ip.getHeight());
		imageStack = filter(ip,sp, imageStack);
		image=new ImagePlus("Weingarten result hw="+(r),imageStack);
		image.show();
	}
	
	@Override
	public void applyFilter(ImageProcessor image, String filterPath,List<Roi> roiList) {

			for (int sigma=sz; sigma<= max_sz; sigma *=2){		
				ImageStack imageStack=new ImageStack(image.getWidth(),image.getHeight());
				GScaleSpace sp=new GScaleSpace(sigma);
				imageStack=filter(image, sp,  imageStack);
				for(int j=1;j<=imageStack.getSize();j++){
					String imageName=filterPath+fs+imageStack.getSliceLabel(j)+".tif" ;
					IJ.save(new ImagePlus(imageStack.getSliceLabel(j), imageStack.getProcessor(j)),imageName );
				}

			}

	}


	
	/**
	 * 
	 * This method is helper function for both applyFilter and run method
	 * @param ip input image
	 * @param sp gaussian scale space
	 * @param sigma filter sigma
	 */
	private ImageStack filter(ImageProcessor ip,GScaleSpace sp,  ImageStack imageStack){

		ip.snapshot();

		if (!isFloat) 
			ip=ip.toFloat(0, null);

		pass++;
		//double sigma=sp.getSigma();
		//System.out.println(settings.get(LEN)+"MG");
		//GScaleSpace sp=new GScaleSpace(sigma);
		float[] kernx= sp.gauss1D();
		//System.out.println("kernx :"+kernx.length);
		GScaleSpace.flip(kernx);		
		float[] kern_diff2= sp.diff2Gauss1D();
		GScaleSpace.flip(kern_diff2);
		//System.out.println("kernx2 :"+kern_diff2.length);
		float[] kern_diff1=sp.diffGauss1D();
		//System.out.println("kernx1:"+kern_diff1.length);
		GScaleSpace.flip(kern_diff1);

		kernel=new float[4][];
		kernel[0]=kernx;
		kernel[1]=kern_diff2;
		kernel[2]=kern_diff1;

		float[] kernel2=sp.computeDiff2Kernel2D();
		kernel[3]=kernel2;
		GScaleSpace.flip(kernel2);  // symmetric but this is the correct way

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

		FloatProcessor fpaux= (FloatProcessor) ip;

		IConv cnv = ConvFactory.createConv();

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

		
		FloatProcessor pamp=new FloatProcessor(width, height); // amplitude of gradient
		FloatProcessor sin_phase=new FloatProcessor(width, height); // phase of gradient
		FloatProcessor cos_phase=new FloatProcessor(width, height); // phase of gradient
		
		FloatProcessor eigen1=new FloatProcessor(width, height); // eigenvalue 1
		FloatProcessor eigen2=new FloatProcessor(width, height); // eigenvalue 2
		
		FloatProcessor hesdet=new FloatProcessor(width, height); //  determinant

		FloatProcessor luv=new FloatProcessor(width, height); // trace
		
		for (int i=0; i<width*height; i++) {
			double gx=gradx.getf(i);
			double gy=grady.getf(i);

			/*
			 *  components of the Hessian
			 */
			double gxy=lap_xy.getf(i);

			double gxx=lap_xx.getf(i);
			double gyy=lap_yy.getf(i);
			
			// Weingarten map
			final double trace = gyy*(1.+ gy*gy) + gxx*(1.+ gx*gx)-2.*gx*gy*gxy;
			final double det = (gxx * gyy - gxy * gxy)*(gy*gy+gx*gx+1.);
			final double disc = sqrt(abs(trace * trace - 4.0 * det));
			double fr=1. + gx*gx +gy*gy;
			fr=Math.pow(fr, 1.5);
			final double ee1=0.5*(trace+disc);
			final double ee2=0.5*(trace-disc);

			// non scaled
			//double guu = gx * gx * gyy  +gy * gy * gxx  - 2.0 * gx *gy *gxy;
			//double gvv = gx * gx * gyy  +gy * gy * gxx  + 2.0 * gx *gy *gxy;
			//double guv = gx * gy * (gxx - gyy) - gxy * (gx * gx - gy * gy);


			//gx*=gx;
			//gy*=gy;		
		 		
			double amp=sqrt(gx*gx+gy*gy);
			if (amp==0) amp+=1e-6;
			
			pamp.setf(i, (float)  (amp));
			
			double gsin= (gy/amp);
				
			sin_phase.setf(i, (float) gsin);
			double gcos= (gx/amp);
		
			cos_phase.setf(i, (float) gcos);
			eigen1.setf(i, (float) ee1);
			eigen2.setf(i, (float) ee2);
			hesdet.setf(i, (float) det);	
			//luv.setf(i, (float) guv);
			//luv.setf(i, (float) (guu*guu- gvv* gvv));
			luv.setf(i, (float) (0.5*trace));
		}
		String fkey=this.getKey();
		
		/*
		if (fulloutput) {
			imageStack.addSlice(fkey+"_X_diff_"+sz, gradx);
			imageStack.addSlice(fkey+"_Y_diff_"+sz, grady);
			imageStack.addSlice(fkey+"_XX_diff_"+sz, lap_xx);
			imageStack.addSlice(fkey+"_YY_diff_"+sz, lap_yy);
			imageStack.addSlice(fkey+"_XY_diff_"+sz, lap_xy);
		}
		 */
		if (fulloutput) {
			imageStack.addSlice(fkey+"_Amp_"+sz, pamp);
			imageStack.addSlice(fkey+"_Sin_"+sz, sin_phase);
			imageStack.addSlice(fkey+"_Cos_"+sz, cos_phase);
		}
		imageStack.addSlice(fkey+"_Det_"+sz, hesdet); 
		imageStack.addSlice(fkey+"_E1_"+sz, eigen1);
		imageStack.addSlice(fkey+"_E2_"+sz, eigen2);
		imageStack.addSlice(fkey+"_Tr_"+sz, luv); 
		luv.resetMinAndMax();
 
		return imageStack;
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
		GenericDialog gd=new GenericDialog("Weingarten " + version);

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
	public void savePreferences(Properties prefs) {
		prefs.put(LEN, Integer.toString(sz));
		prefs.put(FULL_OUTPUT, Boolean.toString(fulloutput));
		// prefs.put(SIGMA, Float.toString(sigma));
	}

	@Override
	public Map<String, String> getDefaultSettings() {

		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		settings.put(FULL_OUTPUT, Boolean.toString(fulloutput));

		return this.settings;
	}

	@Override
	public boolean reset() {
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		fulloutput= Prefs.getBoolean(FULL_OUTPUT, true);
		return true;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		try {
			sz=Integer.parseInt(settingsMap.get(LEN));
			max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
			fulloutput= Boolean.parseBoolean(settingsMap.get(FULL_OUTPUT));
			
			return true;
		} catch ( NumberFormatException ex) {
			return false;
		}
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.isEnabled= isEnabled;
	}

	private double logKernel(double x){
		final double x2=x*x;
		return (x2-2)* exp(-0.5*x2)/(2.0*sqrt(PI));
	}
	
	@Override
	public double[][] kernelData() {
		final int n=50;
		double [][] data=new double[2][n];
		data[0]=SUtils.linspace(-10.0, 10.0, n);
		for(int i=0; i<n; i++){
			data[1][i]=logKernel(data[0][i]);
		}
		return data;
	}


	/*
	 * testing method
	 */
	public static void main (String[] args) {
		Ridge_Filter_ filter=new Ridge_Filter_();
		System.out.println("annotated fields");
		System.out.println(filter.getAnotatedFileds());
	}


}
