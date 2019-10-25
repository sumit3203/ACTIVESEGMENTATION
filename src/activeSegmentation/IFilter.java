package activeSegmentation;


import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IFilter {
	
	// IFSEGM - segmentation: one to one NxN -> NxN
	// IFCLASS - segmentation: many to one NxN -> 1
	public static final int IFSEGM=1, IFCLASS=2;
	
	/**
	 * Returns a new default settings map for the filter 
	 * 
	 * @return a new map.
	 */
	public Map< String, String > getDefaultSettings();
	
	/**
	 * Returns true if setting are update Successfully
	 * @param  settingsMap
	 * @return boolean
	 */
	public boolean updateSettings(Map< String, String > settingsMap);

	
	
	/**
	 * Returns apply filter
	 * 
	 * @return String
	 */	
	//public <T> T applyFilter(ImageProcessor imageProcessor);
	
	
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList);
	
	/**
	 * Returns a unique key of filter
	 * 
	 * @return String
	 */
	public String getKey();
	
	/**
	 * Returns a Name of the filter
	 * 
	 * @return Integer
	 */
	public String getName();

	
	
	/**
	 * Get ive image
	 * @return Image
	 */
	public Image getImage();
	
	/**
	 * Get ive image
	 * @return Image
	 */
	public boolean isEnabled();
	
	/**
	 * Reset all setting of filters
	 */
	public boolean reset();
	


	public void setEnabled(boolean isEnabled);
	
	public int getFilterType();
	
	public <T> T getFeatures();
	public Set<String> getFeatureNames();

	
}
