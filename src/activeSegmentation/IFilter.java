package activeSegmentation;


import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.filter.LoG_Filter_;

/**
 * * 
 * @author Sumit Kumar Vohra, ZIB and Dimiter Prodanov, IMEC
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
public interface IFilter extends IAnnotated {
	
	
	/**
	 * Returns a new default settings map for the filter 
	 * 
	 * @return a new map storing information about filter parameters and their numerical default value in a form of strings imported from ij.Prefs class
	 */
	public Map<String, String> getDefaultSettings();
	
	/**
	 * Returns true if setting are updated successfully
	 * @param  settingsMap a map of Strings storing information about settings with their new, customized values
	 * @return boolean value indicating if settings has been updated or has not
	 */
	public boolean updateSettings(Map<String, String> settingsMap);

	
	/**
	 *
	 * Applies a filter to
	 * @param image an instance of an ImageProcessor  - contains the pixel data of a 2D image
	 * @param path - folder path in which the output is stored
	 * @param roiList - allows for working on ROI level, rather than on each pixel
	 *
	 */	
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList);
	
	/**
	 * returns a unique key of filter
	 * @return String containing the key
	 */
	public default String getKey() {
		Pair<String,String> p=getKeyVal();
		return p.first;
	}
	
	/**
	 * Returns the long name of the filter
	 * @return String  
	 */
	public default String getName() {
		Pair<String,String> p = getKeyVal();
		return p.second;
	}

	
	/**
	 * checks if the filter is used
	 * @return boolean value indicating if the filter is enabled or not
	 */
	public boolean isEnabled();
	
	/**
	 * resets settings to the default values
	 * @return boolean value containing information whether or not the settings has been successfully reset
	 */
	public boolean reset();
	
	
	/**
	 * changes the state of a filter - either to enabled or disabled
	 */
	public void setEnabled(boolean isEnabled);
	
	
	/**
	 * returns the type of filter as an {@link Enumeration}
	 * @return FilterType of a specific filter applied
	 */
	default public FilterType getFilterType() {
		return FilterType.SEGM;
	}
	
	/**
	 * will provide filter specific help message
	 * @return
	 */
	default public String helpInfo() {
		return "This is a filter";
	}

	
}
