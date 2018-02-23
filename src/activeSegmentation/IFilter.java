package activeSegmentation;


import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.util.Map;

public interface IFilter {
	
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
	 * Tell the filter for which slice index it is computed the values.
	 * @param position_id
	 */
	public void updatePosition(int position_id);
	
	/**
	 * Returns apply filter
	 * 
	 * @return String
	 */	
	public <T> T applyFilter(ImageProcessor imageProcessor);
	
	
	public void applyFilter(ImageProcessor image, String path);
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
	 * Get stack size
	 * @return number of slices in the stack
	 */
	public int getSize();
	
	/**
	 * Get slice label
	 * @param index slice index (from 1 to max size)
	 * @return slice label
	 */
	public String getSliceLabel(int index);
	
	/**
	 * Get stack height
	 * @return stack height
	 */
	public int getHeight();
	
	/**
	 * Get stack width
	 * @return stack width
	 */
	public int getWidth();
	
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
	
	/**
	 * Get ive image
	 * @return Image
	 */

	public void setEnabled(boolean isEnabled);

	public ImageStack getImageStack();

	public void setImageStack(ImageStack imageStack);
	
	/**
	 * @return Zernike Polynomial parameters
	 * It can be extended by returning list of parameters when other filter is used
	 */
	public int getDegree();
	
}
