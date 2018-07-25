package activeSegmentation.filter;


import activeSegmentation.IFilter;
import activeSegmentation.filter.GLCMTextureDescriptors;
import ij.IJ;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 */


public class GLCM_filter_ implements IFilter {

	public static boolean debug=IJ.debugMode;
	private boolean isEnabled=true;
	public static final int [] DIRECTIONS = {270,360,90,180};
	public static final int [] DISTANCES = {1};
	public static final String ASM_FEATURE_KEY = "ASM";
	public static final String CONTRAST_FEATURE_KEY = "contrast";
	public static final String CORRELATION_FEATURE_KEY = "correlation";
	public static final String DISSIMILARITY_FEATURE_KEY = "dissimilarity";
	public static final String ENERGY_FEATURE_KEY = "energy";
	public static final String ENTROPY_FEATURE_KEY = "entropy";
	public static final String HOMOGENEITY_FEATURE_KEY = "homogeneity";
	//private ArrayList<Pair<String,Pair<String[],Double[]>>> feature_vector = new ArrayList<>();
	
	private ArrayList<Pair<String,double[]>> feature_vector = new ArrayList<Pair<String,double[]>>();
	private Set<String> features=new HashSet<String>();

	/* NEW VARIABLES*/

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "GLCM";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "Texture Descriptors Filter";

	private final int TYPE=2;
	// 1 Means Segmentation
	// 2 Means Classification

	private Map<String, String> settings= new HashMap<>();


	public void filter(ImageProcessor ip,String roi_name){

		GLCMTextureDescriptors glcm = new GLCMTextureDescriptors();
		
		//features has DIRECTIONS*DISTANCES*NO_OF_DESCRIPTORS (HERE 4*1*7) size
		double[] moment_values = new double[features.size()];
		int k = 0;

		// For all directions and distances calculate GLCM descriptors for a given roi

		for (int angle: DIRECTIONS){
			for(int distance:DISTANCES){
				
				// Using same GLCM object by changing parameters
				glcm.set_values(distance,angle);

				// calculate normalised GLCM matrix for a given roi & d & angle
				glcm.extractGLCMDescriptors(ip);

				// using this glcm matrix we calculate values of different features in GLCMTextureDescriptors
				moment_values[k] = glcm.getAngular2ndMoment();
				k++;
				moment_values[k] = glcm.getContrast();
				k++;
				moment_values[k] = glcm.getCorrelation();
				k++;
				moment_values[k] = glcm.getDissimilarity();
				k++;
				moment_values[k] = glcm.getEnergy();
				k++;
				moment_values[k] = glcm.getEntropy();
				k++;
				moment_values[k] = glcm.getHomogeneity();
				k++;				
			}
		}
		
        //roi moment has name of roi and all the features (all 7 feature descriptors) coming out of this filter 
        Pair<String,double[]> roi_moment = new Pair<>(roi_name,moment_values);
        feature_vector.add(roi_moment);
    }

	/* Saves the current settings of the plugin for further use
	 * 
	 *
	 * @param prefs
	 */
	public void savePreferences(Properties prefs) {
		//prefs.put(DEGREE, Integer.toString(degree));
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		//settings.put(DEGREE, Integer.toString(degree));
		return this.settings;
	}

	@Override
	public boolean reset() {
		//degree= Prefs.getInt(DEGREE, 3);
		return true;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		//degree=Integer.parseInt(settingsMap.get(DEGREE));
		return true;
	}

	@Override
	public void applyFilter(ImageProcessor imageProcessor, String s, List<Roi> list) {
		
		for (int angle: DIRECTIONS){
			for(int distance:DISTANCES){
				// 7 feature Descriptors
				features.add(ASM_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
				features.add(CONTRAST_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
				features.add(CORRELATION_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
				features.add(DISSIMILARITY_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
				features.add(ENERGY_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
				features.add(ENTROPY_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
				features.add(HOMOGENEITY_FEATURE_KEY+"_"+angle+"_"+distance+"_Real");
			}
		}

        // if asked for GLCM descriptors of ROIs
        if(list != null &&  list.size()>0){
            for(int i=0;i<list.size();i++){
                imageProcessor.setRoi(list.get(i));
                ImageProcessor ip_roi = imageProcessor.crop();
                //utility.display_image(ip_roi);
                filter(ip_roi,list.get(i).getName());
            }
        }
		// if asked for GLCM of image, we do not have any use case where we need both at a time
		else{
            System.out.println("Image is not null");
			filter(imageProcessor,s);
		}

	}

	@Override
	public String getKey() {
		return this.FILTER_KEY;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.FILTER_NAME;
	}

	@Override
	public Image getImage(){
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return isEnabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub
		this.isEnabled= isEnabled;
	}

	@Override
	public int getFilterType() {
		// TODO Auto-generated method stub
		return this.TYPE;
	}

	@Override
	public ArrayList<Pair<String,double[]>> getFeatures() {
		// TODO Auto-generated method stub
		return feature_vector;
	}

	@Override
	public Set<String> getFeatureNames() {
		// TODO Auto-generated method stub
		return this.features;
	}

}
