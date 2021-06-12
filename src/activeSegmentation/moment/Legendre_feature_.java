package activeSegmentation.moment;


import activeSegmentation.AFilter;
import activeSegmentation.FilterType;
import activeSegmentation.IMoment;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;

import static activeSegmentation.FilterType.CLASSIF;

import java.util.*;
import java.util.List;


/**
 */

@AFilter(key="LM", value="Legendre Moments", type=CLASSIF)
public class Legendre_feature_ implements IMoment<ArrayList<?>> {

	public static boolean debug=IJ.debugMode;
	public final static String DEGREE = "Degree";

	private  int degree = Prefs.getInt(DEGREE, 6);
	private boolean isEnabled=true;

	private ArrayList<Pair<String,double[]>> moment_vector = new ArrayList<>();
	private Set<String> features=new HashSet<>();

 
	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "LM";
	public static final String LM_FEATURE_KEY = "LM";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "Legendre Moments";

	//private final int TYPE=2;
	// 1 Means Segmentation
	// 2 Means Classification

	private Map<String, String> settings= new HashMap<>();


	public Pair<String,double[]> filter(ImageProcessor ip,String roi_name){
		ImagePlus imp = new ImagePlus("templegrende", ip);
		ImageConverter ic= new ImageConverter(imp);
	    ic.convertToGray8();
	    ip=imp.getProcessor();
        double moment_matrix[][] = new LegendreMoments_elm(degree,degree).extractLegendreMoment(ip);
        double[] moment_values = new double[features.size()];
        int k = 0;
        for(int i=0;i<=degree;i++){
            for(int j=0;j<=degree;j++){
            	// in moment_values we have moments of all orders(m,n) for one particular roi
				moment_values[k] = moment_matrix[i][j];
				k++;
            }
        }
        
        //roi moment has name of roi and all the features (all degree moment values) coming out of this filter 
        Pair<String,double[]> roi_moment = new Pair<>(roi_name,moment_values);
        moment_vector.add(roi_moment);
        return roi_moment;
    }

	/* Saves the current settings of the plugin for further use
	 * 
	 *
	 * @param prefs
	 */
	public void savePreferences(Properties prefs) {
		prefs.put(DEGREE, Integer.toString(degree));
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		settings.put(DEGREE, Integer.toString(degree));
		return this.settings;
	}

	@Override
	public boolean reset() {
		degree= Prefs.getInt(DEGREE, 3);
		return true;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		degree=Integer.parseInt(settingsMap.get(DEGREE));
		return true;
	}

	@Override
	public void applyFilter(ImageProcessor imageProcessor, String s, List<Roi> list) {
		for(int i=0;i<=degree;i++){
            for(int j=0;j<=degree;j++){
				features.add(LM_FEATURE_KEY+"_"+i+"_"+j+"_Real");				
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

	
	public void generateFeatures() {
		for(int i=0;i<=degree;i++){
            for(int j=0;j<=degree;j++){
				features.add(LM_FEATURE_KEY+"_"+i+"_"+j+"_Real");				
            }
       }
	}
	public Pair<String,double[]> apply(ImageProcessor imageProcessor, Roi roi) {
		
		
      return filter(imageProcessor, roi.getName());

	}
	@Override
	public String getKey() {
		return this.FILTER_KEY;
	}

	@Override
	public String getName() {
		return this.FILTER_NAME;
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
	public FilterType getFilterType() {
		return FilterType.CLASSIF;
	}

	@Override
	public ArrayList<Pair<String,double[]>> getFeatures() {
		return moment_vector;
	}

	@Override
	public Set<String> getFeatureNames() {
		return this.features;
	}

}
