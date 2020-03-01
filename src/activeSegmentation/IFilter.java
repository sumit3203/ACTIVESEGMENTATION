package activeSegmentation;


import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents abstract filter description
 * 
 * both classification and segmentation
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
public interface IFilter {
	
	// IFSEGM - segmentation: one to one NxN -> NxN
	// IFCLASS - segmentation: many to one NxN -> 1
	// eventually move into enum
	public static final int IFSEGM=1, IFCLASS=2;
	
	/**
	 * Returns a new default settings map for the filter 
	 * 
	 * @return a new map.
	 */
	public Map<String, String> getDefaultSettings();
	
	/**
	 * Returns true if setting are update Successfully
	 * @param  settingsMap
	 * @return boolean
	 */
	public boolean updateSettings(Map<String, String> settingsMap);

	
	
	/**
	 * Returns apply filter
	 * 
	 * @return String
	 */	
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList);
	
	/**
	 * returns a unique key of filter
	 *  // to be changed for UID use
	 * @return String
	 */
	public String getKey();
	
	/**
	 * Returns the name of the filter
	 * 
	 * @return Integer
	 */
	public String getName();

	
	/**
	 *  returns the plot of the filter kernel
	 * @return Image
	 */
	public Image getImage();
	
	/**
	 * checks if the filter is used
	 * @return Image
	 */
	public boolean isEnabled();
	
	/**
	 * resets all setting of filters
	 */
	public boolean reset();
	
	
	/**
	 * changes the state
	 * @return Image
	 */
	public void setEnabled(boolean isEnabled);
	
	/**
	 * change into enum
	 */
	public int getFilterType();
	
	/**
	 * used in for loops  -> typing on method level necessary
	 */
	public <T> T getFeatures();
	
	/**
	 * names of features must be unique
	 */
	public Set<String> getFeatureNames();

	
}
