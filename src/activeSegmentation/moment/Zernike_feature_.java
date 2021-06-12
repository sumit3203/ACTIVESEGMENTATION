package activeSegmentation.moment;

import static activeSegmentation.FilterType.CLASSIF;

import java.awt.AWTEvent;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.AFilter;
import activeSegmentation.FilterType;
import activeSegmentation.IFilter;
import activeSegmentation.IMoment;
import ij.ImagePlus;
//import ij.ImageStack;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Roi;
//import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilter;
//import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ijaux.datatype.ComplexArray;
import ijaux.moments.zernike.ZernikeMoment;
//import ijaux.moments.ZernikeMoment.ComplexWrapper;
import ijaux.scale.Pair;

@AFilter(key="ZMC", value="Zernike Moments", type=CLASSIF)
public class Zernike_feature_ implements PlugInFilter, DialogListener, IMoment<ArrayList<?>> {

	final int flags=DOES_ALL+ NO_CHANGES;
	public final static String DEG="Degree";
	private int degree= Prefs.getInt(DEG, 6);
 
	private ArrayList<Pair<String,double[]>> moment_vector = new ArrayList<Pair<String,double[]>>();
	private Set<String> features=new HashSet<String>();

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "ZMC";
	private final static String ZM_FEATURE_KEY = "ZM";
	
  	
	/** It stores the settings of the Filter. */
	private Map< String, String > settings= new HashMap<String, String>();
	
	private boolean isEnabled=true;

 
	
	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "Zernike Moments";
 
	
	ZernikeMoment zm = null;
	
	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		//imageStack=new ImageStack(ip.getWidth(),ip.getHeight());

		//img.show();
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		//this.img=arg1;
		return flags;
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		settings.put(DEG, Integer.toString(4));
		return this.settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		degree = Integer.parseInt(settingsMap.get(DEG));
		return true;
	}

	
	@Override
	public void applyFilter(ImageProcessor imageProcessor, String s,List<Roi> list) {

		ImagePlus imp = new ImagePlus("tempglcm", imageProcessor);
		ImageConverter ic= new ImageConverter(imp);
	    ic.convertToGray8();
	    imageProcessor=imp.getProcessor();
		for(int i=0;i<=degree;i++){
			for(int j=0;j<=i;j++){
				if((i-j)%2==0){
					features.add(ZM_FEATURE_KEY+"_"+i+"_"+j+"_Real");
					features.add(ZM_FEATURE_KEY+"_"+i+"_"+j+"_Imag");
				}
			}
		}
		
		// if asked for moment of ROIs
		if(list != null && list.size()>0){
			for(int i=0;i<list.size();i++){
				imageProcessor.setRoi(list.get(i));
				ImageProcessor ip_roi = imageProcessor.crop();
				//utility.display_image(ip_roi);
				filter(ip_roi,list.get(i).getName());
			}
		}

		// if asked for moment of image, we do not have any use case where we need both at a time
		else{
			filter(imageProcessor,s);
		}

	}
	
	public Pair<String,double[]> apply(ImageProcessor imageProcessor, Roi roi) {
		
		return filter(imageProcessor,  roi.getName());
	}

	public void generateFeatures() {
		for(int i=0;i<=degree;i++){
			for(int j=0;j<=i;j++){
				if((i-j)%2==0){
					features.add(ZM_FEATURE_KEY+"_"+i+"_"+j+"_Real");
					features.add(ZM_FEATURE_KEY+"_"+i+"_"+j+"_Imag");
				}
			}
		}
		
	}
	/**
	 * 
	 * This method is helper function for both applyFilter and run method
	 * @param ip input image
	 */
	private Pair<String,double[]>  filter(ImageProcessor ip,String roi_name){

		ComplexArray cp = new ZernikeMoment(degree).extractZernikeMoment(ip);
		int counter = 0;
		int k=0;
		double[] moment_values = new double[features.size()];
		for(int i=0;i<=degree;i++){
			for(int j=0;j<=i;j++){
				if((i-j)%2==0){
				/*	String[] order_index = new String[3];
					Double[] moment_values = new Double[2];
					Pair<String[],Double[]> order = new Pair<>(order_index,moment_values);
					Pair<String,Pair<String[],Double[]>> one_roi_moment = new Pair<>("",order);
					order_index[0] = ZM_FEATURE_KEY;
					order_index[1] = Integer.toString(i);
					order_index[2] = Integer.toString(j);
					order.first = order_index;
					System.out.println("counter "+counter);*/
					
					moment_values[k] = cp.Re(counter);
					k++;
					moment_values[k] = cp.Im(counter);
					counter++;
					k++;
					
					
					
				}
			}
		}

		Pair<String,double[]> roi_moment = new Pair<>(roi_name,moment_values);
		
		moment_vector.add(roi_moment);
		return  roi_moment;
		/*int index = position_id;
		ip.snapshot();
		ip.getDefaultColorModel();
		if(zm==null){
			zm=new ZernikeMoment(degree);
		}
		return new Pair<Integer,Complex>(index, zm.extractZernikeMoment(ip));*/

	}

	public int getDegree(){
		return degree;
	}
	
	@Override
	public String getKey() {
		return FILTER_KEY;
	}

	@Override
	public String getName() {
		return FILTER_NAME;
	}

	 
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public boolean reset() {
		return false;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.isEnabled= isEnabled;
	}


	@Override
	public boolean dialogItemChanged(GenericDialog arg0, AWTEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

 
	@Override
	public FilterType getFilterType() {
		return FilterType.CLASSIF;
	}
 

	@Override
	public ArrayList<Pair<String,double[]>> getFeatures() {
		// TODO Auto-generated method stub
		return moment_vector;
	}

	@Override
	public Set<String> getFeatureNames() {
		// TODO Auto-generated method stub
		return this.features;
	}


}
