package activeSegmentation.filter;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.DialogListener;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.GScaleSpace;
import ijaux.scale.SUtils;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import dsp.Conv;

import static activeSegmentation.FilterType.SEGM;
import static java.lang.Math.*;


/**
 * @version 	1.2.1 31 Oct 2019
 * 				 - kernel plot change
 * 				1.6    23 Aug 2016
 *              1.5		date 23 Sept 2013
 *				- isotropic correction
 * 				1.0		date 23 Jul 2013 
 * 				Based on Mexican_Hat_Filter v 2.2
 * 				- common functionality is refactored in a library class
 * 				
 *   
 * 
 * @author Dimiter Prodanov IMEC  & Sumit Kumar Vohra
 *
 *
 * @contents
 * This plug-in convolves an image with a Bi-Laplacian of Gaussian (BoG) filter
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

@AFilter(key="BOG", value="Bi-Laplacian of Gaussian", type=SEGM)
public class BoG_Filter_ implements ExtendedPlugInFilter, DialogListener, IFilter, IFilterViz {
   
	//////////////////
	// Declarations
	/////////////////  
	private final int flags=DOES_ALL+SUPPORTS_MASKING+KEEP_PREVIEW;
	
	private String version="1.5";
	
	public final static String SIGMA="LOG_sigma", LEN="G_len",MAX_LEN="G_MAX", ISO="G_iso", ISSEP="G_SEP";

	public static boolean debug=IJ.debugMode;
	
	////////////////////
	// Annotated fields
	///////////////////
	
	@AFilterField(key=LEN, value="minimal scale")
	public static int sz= Prefs.getInt(LEN, 2);
	
	@AFilterField(key=MAX_LEN, value="maximal scale")
	public  int max_sz= Prefs.getInt(MAX_LEN, 8);
	
	@AFilterField(key=ISSEP, value="separable")
	public static boolean sep= Prefs.getBoolean(ISSEP, false);
	
	@AFilterField(key=ISO, value="isotropic")
	public static boolean isiso= Prefs.getBoolean(ISO, true);
	
	
	/* PRIVATE FIELDS */
	private float[][] kernel=null;

	private ImagePlus image=null;
	
	private boolean isFloat=false;
	
	@SuppressWarnings("unused")
    private boolean hasRoi=false;
	
	@SuppressWarnings("unused")
	private int nPasses=1;
	private int pass;
	
	@SuppressWarnings("unused")
	private PlugInFilterRunner pfr=null;
	
	private Object pixundo;
	 
	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	//private final  String FILTER_KEY = "BOG";

	/** The pretty name of the target detector. */
 	//private final String FILTER_NAME = "Bi-Laplacian of Gaussian";
	
	private Map< String, String > settings= new HashMap<String, String>();
	
	private boolean isEnabled=true;
	 
	
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
		hasRoi=imp.getRoi()!=null;
		return  flags;
	}

 /*
  * (non-Javadoc)
  * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
  */
	@Override
	public void run(ImageProcessor ip) {
	
		int r = (sz-1)/2;
		//GScaleSpace sp=new GScaleSpace(r);
		GScaleSpace sp=new GScaleSpace(r,3.0f);
		FloatProcessor fp=filter(ip,sp,sep,isiso);
		image.setProcessor(fp);
		image.updateAndDraw();
	}

  
	public static void main (String[] args) {
		BoG_Filter_ filter=new BoG_Filter_();
		
		filter.getDefaultSettings();
		
	}
	
	@Override
	public void applyFilter(ImageProcessor image, String filterPath,List<Roi> roiList) {
		String key=getKey();
		for (int sigma=sz; sigma<= max_sz; sigma *=2){		
			GScaleSpace sp=new GScaleSpace(sigma);
			ImageProcessor fp=filter(image, sp, sep, isiso);
			String imageName=filterPath+"/"+key+"_"+sigma+".tif" ;
			IJ.save(new ImagePlus(key+"_" + sigma, fp),imageName );

		}

	}


	private FloatProcessor filter(ImageProcessor ip,GScaleSpace sp, final boolean seperable,final boolean isotropic){
		
		ip.snapshot();
	 	
		if (!isFloat) 
			ip=ip.toFloat(0, null);

		
		pass++;
		
		float[] kernx= sp.gauss1D();
		GScaleSpace.flip(kernx);		

		float[] kern_diff_4= sp.diffNGauss1D(4);
		GScaleSpace.flip(kern_diff_4);
		
		float[] kern_diff_2= sp.diffNGauss1D(2);
		GScaleSpace.flip(kern_diff_2);		
		
		kernel=new float[4][];
		kernel[0]=kernx;
		kernel[1]=kern_diff_4;
		kernel[2]=kern_diff_2;
		float[] kernel2=sp.computeLapNKernel2D(2); // 2D kernel computation
		kernel[3]=kernel2;
			
		int sz= sp.getSize();
		
		/*if (debug)
			System.out.println("sz " +sz);*/
		
		float[][] disp= new float[3][];

		disp[0]=GScaleSpace.joinXY(kernel, 0, 1);
		disp[1]=GScaleSpace.joinXY(kernel, 1, 0);
		
		
		if (debug && pass==1) {
			FloatProcessor fp=new FloatProcessor(sz,sz);
			if (isotropic) {
				disp[2]=GScaleSpace.joinXY(kernel, 2, 2);
				for (int i=0; i<sz*sz; i++)
					fp.setf(i, disp[0][i]+ disp[1][i] + 2*disp[2][i]  );
				 
			} else {
				for (int i=0; i<sz*sz; i++)
					fp.setf(i, disp[0][i]+ disp[1][i] );
			}
			new ImagePlus("kernel sep",fp).show();
			if (!seperable) {
				FloatProcessor fp2=new FloatProcessor(sz,sz, kernel2);
				new ImagePlus("kernel 2D",fp2).show();
			}
		}
		long time=-System.nanoTime();	
		
		FloatProcessor fpaux= (FloatProcessor) ip;
	 		
		Conv cnv=new Conv();
		if (seperable) {
			if (isotropic) {
				FloatProcessor fpauxiso=(FloatProcessor) fpaux.duplicate();
								
				cnv.convolveSemiSep(fpaux, kernx, kern_diff_4);	
				for (int i=0; i<kern_diff_2.length; i++)
					kern_diff_2[i]*=Math.sqrt(2.0);
				cnv.convolveFloat1D(fpauxiso, kern_diff_2, 0); //Ox
				cnv.convolveFloat1D(fpauxiso, kern_diff_2, 1); //Oy
				
				fpaux.copyBits(fpauxiso, 0, 0, Blitter.ADD);
				//System.out.println("separable & isotropic computation");
			} else {
				cnv.convolveSemiSep(fpaux, kernx, kern_diff_4);	
				//System.out.println("separable & non-isotropic computation");
			}
		} else {	
			if (isotropic) {			
				//System.out.println("non-separable & isotropic computation");
			} else {
				for (int i=0; i<sz*sz; i++)
					kernel2[i]=disp[0][i]+ disp[1][i];
				//System.out.println("non-separable & non-isotropic computation");
			} // end else
			cnv.convolveFloat(fpaux, kernel2, sz, sz);
		} // end else
	 
		time+=System.nanoTime();
		time/=1000.0f;
		//System.out.println("elapsed time: " + time +" us");
		fpaux.resetMinAndMax();	
		
		return fpaux;
	
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
		GenericDialog gd=new GenericDialog("Bi-LoG (BoG) " + version);
		gd.addNumericField("half width", r, 1);
		//gd.addNumericField("sigma", sigma, 1);
		gd.addCheckbox("Show kernel", debug);
		gd.addCheckbox("Separable", sep);
		gd.addCheckbox("isotropic", isiso);
		/*if (hasRoi)
			gd.addCheckbox("Brightness correct", true);*/
		
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		pixundo=imp.getProcessor().getPixelsCopy();
		if (gd.wasCanceled()) {			
			//image.repaintWindow();
			return DONE;
		}
		/*if (!IJ.isMacro())
			staticSZ = sz;*/
		return IJ.setupDialog(imp, flags);
	}
	

 
	
	// Called after modifications to the dialog. Returns true if valid input.
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		int r = (int)(gd.getNextNumber());
		//sigma = (float) (gd.getNextNumber());
		debug = gd.getNextBoolean();
		sep = gd.getNextBoolean();
		isiso = gd.getNextBoolean();
		//convert=gd.getNextBoolean();
		 
		sz = 2*r+1;
		if (gd.wasCanceled()) {
			ImageProcessor proc=image.getProcessor();
			proc.setPixels(pixundo);
			proc.resetMinAndMax();
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
	
	
	/** Saves the current settings of the plugin for further use
    *
    * @param prefs
    */
   public static void savePreferences(Properties prefs) {
  		prefs.put(LEN, Integer.toString(sz));
  		prefs.put(ISO, Boolean.toString(isiso));
  		prefs.put(ISSEP, Boolean.toString(sep));
        // prefs.put(SIGMA, Float.toString(sigma));

   }

   @Override
	public Map<String, String> getDefaultSettings() {

	   Field [] fields = BoG_Filter_.class.getFields();
		System.out.println("fields "+fields.length);
		
		for (Field field:fields)   {
			if (field.isAnnotationPresent(AFilterField.class)) {
				AFilterField fielda =  field.getAnnotation(AFilterField.class);
				//System.out.println(field.toString());
		        System.out.println("key: " + fielda.key() +" value: " + fielda.value());
			}
		}
		
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		settings.put(ISSEP, Boolean.toString(sep));
		settings.put(ISO, Boolean.toString(isiso));

		return this.settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		sep=Boolean.parseBoolean(settingsMap.get(ISSEP));
		isiso=Boolean.parseBoolean(settingsMap.get(ISO));

		return true;
	}
	
	@Override
	public boolean reset() {
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		sep= Prefs.getBoolean(ISSEP, true);
		isiso=Prefs.getBoolean(ISO, true);
		return true;
	}

	
	private double bogKernel(double x){
		final double x2=x*x;
		return (x2*x2-8*(x2)+8)* exp(-0.5*x2) / (2.0*sqrt(PI));
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
			data[1][i]=bogKernel(data[0][i]);
		}
		return data;
	}


}
