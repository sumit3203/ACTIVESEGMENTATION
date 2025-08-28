package activeSegmentation.filter;
import dsp.ConvFactory;
import dsp.IConv;
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
 
import java.util.*;
import java.util.List;

import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import dsp.cpu.Conv;

import static activeSegmentation.FilterType.SEGM;
import static java.lang.Math.*;

/**
 * @version 	
 * 				1.1 13 Feb 2023
 * 				- help annotations
 * 				1.0 4 Jun 2022
 * 				
 *              Isophote curvatures + Streamline curvature
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
 
@AFilter(key="CURVATURE2", value="Curvature 2D", type=SEGM, help = "/help.html")
public class GaussK3_Filter_ implements ExtendedPlugInFilter, DialogListener,IFilter, IFilterViz {

	private PlugInFilterRunner pfr=null;

	final int flags=DOES_ALL+KEEP_PREVIEW+ NO_CHANGES;
	private String version="1.0";
	private int nPasses=1;
	private int pass;
 
	public final static String SIGMA="GK_sigma", LEN="GK_len",  MAX_LEN="GK_MAX",  FULL_OUTPUT="KFull_out";
 
	@AFilterField(key=LEN, value="initial scale")
	public  int sz= Prefs.getInt(LEN, 2);

	@AFilterField(key=MAX_LEN, value="max scale")
	public  int max_sz= Prefs.getInt(MAX_LEN, 8);


	private ImagePlus image=null;
	public static boolean debug=false;//IJ.debugMode;

	@AFilterField(key=FULL_OUTPUT, value="full output")
	public  boolean fulloutput=Prefs.getBoolean(FULL_OUTPUT, false);

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
		cal=image.getCalibration();
		return  flags;
	}

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
		ip.snapshot();
	 	
		if (!isFloat) 
			ip=ip.toFloat(0, null);
		
		pass++;
		
		int r = (sz-1)/2;
		GScaleSpace sp=new GScaleSpace(r);
		//GScaleSpace sp=new GScaleSpace(sigma); 	
		 
		
		ImageStack imageStack=new ImageStack(ip.getWidth(),ip.getHeight());
		
		long time=-System.nanoTime();	
			
		imageStack = filter(ip, sp, imageStack);
		
		
		time+=System.nanoTime();
		time/=1000.0f;
		System.out.println("elapsed time: " + time +" us");
		System.out.println("sigma: " + sp.getSigma() + 
						   " scale: " + sp.getScale() + 
						   " kernel size: "+ sp.getSize()
						   );
	 		
		image=new ImagePlus("Gauss K result hw="+r,imageStack);
		image.show();
 
		image.getProcessor().resetMinAndMax();	
	}


	/**
	 * @param ip
	 * @param sp
	 * @return
	 */
	private ImageStack filter(ImageProcessor ip, GScaleSpace sp, ImageStack is) {
		float[][] kernel=null;
		ip.snapshot();
		if (!isFloat) 
			ip=ip.toFloat(0, null);
		
		 
		
		float[] kernx= sp.gauss1D();
		System.out.println("kernx :"+kernx.length);
		SUtils.flip(kernx);		
		float[] kern_diff2= sp.diff2Gauss1D();
		System.out.println("kern_diff2 :"+kern_diff2.length);
		SUtils.flip(kern_diff2);
		
		float[] kern_diff1=sp.diffGauss1D();
		System.out.println("kern_diff1 :"+kern_diff1.length);
		SUtils.flip(kern_diff1);
		
		kernel=new float[4][];
		kernel[0]=kernx;
		kernel[1]=kern_diff2;
		kernel[2]=kern_diff1;
		
		float[] kernel2=sp.computeDiff2Kernel2D();
		kernel[3]=kernel2;
		SUtils.flip(kernel2);  // symmetric but this is the correct way
		
		int sz= sp.getSize();
		if (debug ) {
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
	
	
		FloatProcessor lap_norm=new FloatProcessor(width, height); // normal 
		FloatProcessor lap_tan=new FloatProcessor(width, height); // tangential
		FloatProcessor hesdet=new FloatProcessor(width, height); // Hessian determinant
		
		for (int i=0; i<width*height; i++) {
			// components of the gradient
			double gx=gradx.getf(i);
			double gy=grady.getf(i);
	
			// components of the Hessian
			double gxy=lap_xy.getf(i);
	
			double gxx=lap_xx.getf(i);
			double gyy=lap_yy.getf(i);
	
			float det= (float) (gxx*gyy- gxy*gxy);
		 		
			double amp=sqrt( gx*gx+gy*gy)+ 1e-6; 
			
			double damp=amp*amp*amp;
		  
			//  Isophote curvature
			// (-2*(F[x])*(F[xy])*(F[y])+(F[xx])*(F[y])^2+(F[x])^2*(F[yy]))/((F[x])^2+(F[y])^2)^(3/2)
			float gk=(float)(   ( gxx*gy*gy + gyy*gx*gx- 2.*gxy*gx*gy ) /damp   );
			
			if (abs(gk) <1e-8) gk=0;
	
	
		    //Streamline curvature
			// ((F[x])^2*(F[xy])-(F[x])*(F[xx])*(F[y])-(F[xy])*(F[y])^2+(F[x])*(F[y])*(F[yy]))/((F[x])^2+(F[y])^2)^(3/2)
			float gt=(float)(   (  gx*gy*(gyy-gxx) + gxy*(gx*gx - gy*gy) ) /damp  );
			if (abs(gt) <1e-8) gt=0;
			
			lap_tan.setf(i, gt);
			lap_norm.setf(i, gk);
			hesdet.setf(i, det);
		}
		
		String fkey=this.getKey();
		
		if (fulloutput) {
			is.addSlice(fkey+"_X_diff_"+sz, gradx);
			is.addSlice(fkey+"_Y_diff_"+sz, grady);
			is.addSlice(fkey+"_XX_diff_"+sz, lap_xx);
			is.addSlice(fkey+"_YY_diff_"+sz, lap_yy);
			is.addSlice(fkey+"_XY_diff_"+sz, lap_xy);
		}
	
		is.addSlice(fkey+"_K_n_"+sz, lap_norm);
		 
		is.addSlice(fkey+"_K_t_"+sz, lap_tan);
		is.addSlice(fkey+"_Hess_det_"+sz, hesdet);
	 
		return is;
	}

	
	
 	

	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
	 */
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		int r = (sz-1)/2;
		GenericDialog gd=new GenericDialog("Iso/Stream Curvature " + version);
	
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
   public  void savePreferences(Properties prefs) {
	   		prefs.put(LEN, Integer.toString(sz));
	   		prefs.put(FULL_OUTPUT, Boolean.toString(fulloutput));
	   		prefs.put(MAX_LEN, Integer.toString(max_sz));
         // prefs.put(SIGMA, Float.toString(sigma));

   }
   
   /*
	 * @param args - args[0] should point to the folder where the plugins are installed 
	 */
	public static void main(String[] args) {

		new ImageJ();
		IJ.run("Blobs (25K)");
		ImageProcessor ip=IJ.getImage().getProcessor();
			
		GaussK3_Filter_ mf=new GaussK3_Filter_();
	 	 
		mf.sz=21;

		mf.run(ip);
		

	}


	@Override
	public Map<String, String> getDefaultSettings() {
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		settings.put(FULL_OUTPUT, Boolean.toString(fulloutput));

		return this.settings;
	}


	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		try {
			sz=Integer.parseInt(settingsMap.get(LEN));
			max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
			fulloutput= Boolean.parseBoolean(settingsMap.get(FULL_OUTPUT));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	@Override
	public void applyFilter(ImageProcessor image, String filterPath, List<Roi> roiList) {
		for (int sigma=sz; sigma<= max_sz; sigma *=2){		
			ImageStack imageStack=new ImageStack(image.getWidth(),image.getHeight());
			GScaleSpace sp=new GScaleSpace(sigma);
			imageStack=filter(image, sp,  imageStack);
			for(int j=1;j<=imageStack.getSize();j++){
				String imageName=filterPath+"/"+imageStack.getSliceLabel(j)+".tif" ;
				IJ.save(new ImagePlus(imageStack.getSliceLabel(j), imageStack.getProcessor(j)),imageName );
			}

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

	@Override
	public boolean reset() {
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		fulloutput=Prefs.getBoolean(FULL_OUTPUT, false);
		return true;
	}

	private double logKernel(double x){
		final double x2=x*x;
		return (x2-2)* exp(-0.5*x2)/(2.0*sqrt(PI));
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
