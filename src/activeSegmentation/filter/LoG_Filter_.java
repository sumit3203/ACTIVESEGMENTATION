package activeSegmentation.filter;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.DialogListener;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;
import ijaux.scale.GScaleSpace;
import ijaux.scale.SUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;



import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import dsp.Conv;

import static activeSegmentation.FilterType.*;
import static java.lang.Math.*;

/**
 * @version 	1.3.0 13 April 2020
 * 				- plotting mechanism moved to the interface
 * 
 * 				1.2.1 31 Oct 2019
 * 				 - kernel plot change
 * 				1.2 23 Aug 2016
 *              1.1	14 Oct 2013
 * 				- moved contratAdjust -> Conv
 * 				- changed brightness adjustment factor to sigma^2		
 * 				1.1 	18 Jul 2013
 * 				- refactoring
 * 				1.0		05 Feb 2013 
 * 				Based on Mexican_Hat_Filter v 2.2
 * 				- common functionality is refactored in a library class
 * 				
 *   
 * 
 * @author Dimiter Prodanov IMEC , Sumit Kumar Vohra
 *
 *
 * @contents
 * This pluign convolves an image with a Mexican Hat (Laplacian of Gaussian, LoG) filter
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


@AFilter(key="LOG", value="Laplacian of Gaussian", type=SEGM)
public class LoG_Filter_ implements ExtendedPlugInFilter, DialogListener, IFilter, IFilterViz {
	@SuppressWarnings("unused")

	private PlugInFilterRunner pfr=null;

	private final int flags=DOES_ALL+SUPPORTS_MASKING+KEEP_PREVIEW;
	private String version="2.1";
	@SuppressWarnings("unused")

	private int nPasses=1;

	public static boolean debug=IJ.debugMode;
	
	public final static String SIGMA="LOG_sigma", LEN="G_len", MAX_LEN="G_MAX", ISSEP="G_SEP", SCNORM="G_SCNORM";

	@AFilterField(key=ISSEP, value="separable")
	public boolean sep= Prefs.getBoolean(ISSEP, true);

	@AFilterField(key=SCNORM, value="normalized")
	public boolean scnorm= Prefs.getBoolean(SCNORM, false);
	
	@AFilterField(key=LEN, value="initial scale")
	public int sz= Prefs.getInt(LEN, 2);
	
	@AFilterField(key=MAX_LEN, value="max scale")
	public int max_sz= Prefs.getInt(MAX_LEN, 8);


	private ImagePlus image=null;	
	private boolean isFloat=false;	
	private boolean hasRoi=false;
	private Object pixundo;
	private boolean convert=false;
	

	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	//private  String FILTER_KEY = "LOG";

	/** The pretty name of the target detector. */
	//private  String FILTER_NAME = "Laplacian of Gaussian";

	private Map< String, String > settings= new HashMap<String, String>();
	
	private boolean isEnabled=true;
	
	/**
	 * 
	 *	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		image=imp;
		isFloat= (image.getType()==ImagePlus.GRAY32);
		hasRoi=imp.getRoi()!=null;
		return  flags;
	}


	@Override
	public void run(ImageProcessor ip) {
		int r = sz;//(sz-1)/2;
		GScaleSpace sp=new GScaleSpace(r);
		FloatProcessor fp=filter(ip,sp,sep,scnorm);
		image.setProcessor(fp);
		image.updateAndDraw();
	}

	
	@Override
	public void applyFilter(ImageProcessor image, String filterPath,List<Roi> roiList) {
		String key=getKey();	
		for (int sigma=sz; sigma<= max_sz; sigma *=2){		
				GScaleSpace sp=new GScaleSpace(sigma);
				ImageProcessor fp=filter(image, sp,sep, scnorm);
				String imageName=filterPath+"/"+key+"_"+sigma+".tif" ;
				IJ.save(new ImagePlus(key+"_" + sigma, fp),imageName );

			}

	}

	private FloatProcessor filter(ImageProcessor ip, GScaleSpace sp, final boolean seperable,final boolean snorm){
		float[][] kernel=null;
		ip.snapshot();

		if (!isFloat) 
			ip=ip.toFloat(0, null);

		float[] kernx= sp.gauss1D();
		GScaleSpace.flip(kernx);		
		float[] kern_diff= sp.diff2Gauss1D();
		GScaleSpace.flip(kern_diff);

		//System.out.println("scnorm "+snorm);
		if (snorm) {
			double gamma=sp.getSigma(); 	 
			for (int i=0; i<kern_diff.length; i++) {
				kern_diff[i]=(float) (kern_diff[i]*gamma);
				kernx[i]=(float) (kernx[i]*gamma);
			}
		}
		kernel=new float[3][];
		kernel[0]=kernx;
		kernel[1]=kern_diff;

		float[] kernel2=sp.computeDiff2Kernel2D();
		if (snorm) {
			double gamma=sp.getScale();
			for (int i=0; i<kern_diff.length; i++) {
				kernel2[i]=(float) (kernel2[i]*gamma);
			}
		}
		kernel[2]=kernel2;
		GScaleSpace.flip(kernel2);  // symmetric but this is the correct way

		int sz= sp.getSize();
		long time=-System.nanoTime();	

		FloatProcessor fpaux= (FloatProcessor) ip;

		Conv cnv=new Conv();
		if (seperable) {
			//System.out.println("SEPRABLE");
			cnv.convolveSemiSep(fpaux, kernx, kern_diff);			
		} else {		 
			cnv.convolveFloat(fpaux, kernel2, sz, sz);
		}

		time+=System.nanoTime();
		time/=1000.0f;
		//System.out.println("elapsed time: " + time +" us");
		fpaux.resetMinAndMax();	

		if (convert) {

			final double d1=0;
			final double dr=sp. getScale();	
			//System.out.println("linear contrast adjustment y=ax+b \n " +" b= " +d1 +" a= " + dr);

			Conv.contrastAdjust(fpaux, dr, d1);
		}

		return fpaux;


	}

	

	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
	 */
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		int r = (sz-1)/2;
		GenericDialog gd=new GenericDialog("Mex. Hat " + version);
		gd.addNumericField("hw", r, 1);
		gd.addCheckbox("Show kernel", debug);
		gd.addCheckbox("Separable", sep);
		gd.addCheckbox("Scale normalize", scnorm);
		if (hasRoi)
			gd.addCheckbox("Brightness correct", true);

		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();

		pixundo=imp.getProcessor().getPixelsCopy();
		if (gd.wasCanceled()) {			
			return DONE;
		}

		return IJ.setupDialog(imp, flags);
	}



	// Called after modifications to the dialog. Returns true if valid input.
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		sz = (int)(gd.getNextNumber());
		debug = gd.getNextBoolean();
		sep = gd.getNextBoolean();
		scnorm = gd.getNextBoolean();
		convert=gd.getNextBoolean();
		if (gd.wasCanceled()) {
			ImageProcessor proc=image.getProcessor();
			proc.setPixels(pixundo);
			proc.resetMinAndMax();
			return false;
		}
		return sz>0;
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
		prefs.put(ISSEP, Boolean.toString(sep));
		prefs.put(SCNORM, Boolean.toString(scnorm));

	}

	
	
	@Override
	public Map<String, String> getDefaultSettings() {
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		settings.put(ISSEP, Boolean.toString(sep));
		settings.put(SCNORM, Boolean.toString(scnorm));
		return this.settings;
	}

	@Override
	public boolean reset() {
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		sep= Prefs.getBoolean(ISSEP, true);
		return true;
	}


	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		try {
			sz=Integer.parseInt(settingsMap.get(LEN));
			max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
			sep=Boolean.parseBoolean(settingsMap.get(ISSEP));
			scnorm=Boolean.parseBoolean(settingsMap.get(SCNORM));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

	
	public static void main (String[] args) {
		LoG_Filter_ filter=new LoG_Filter_();
		filter.getAnotatedFileds();
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
