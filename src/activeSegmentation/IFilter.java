package activeSegmentation;


import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.filter.AFilterField;
import activeSegmentation.filter.LoG_Filter_;

/**
 * * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov, IMEC
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
	//public static final int IFSEGM=1, IFCLASS=2;
	
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
	 * Filter type: segmentation or classification
	 */
	default public FilterType getFilterType() {
		return FilterType.SEGM;
	}
	
	/**
	 * used in for loops  -> typing on method level necessary
	 */
	public <T> T getFeatures();
	
	/**
	 * names of features must be unique
	 */
	public Set<String> getFeatureNames();

	
	/*
	 * returns annotations of the public(!) fields
	 */
	default Map<String, String> getAnotatedFileds(){		
		Map< String, String > afields= new HashMap<String, String>();
		Field [] fields = this.getClass().getFields();
		for (Field field:fields)   {
			if (field.isAnnotationPresent(AFilterField.class)) {
				AFilterField fielda =  field.getAnnotation(AFilterField.class);
		        //System.out.println("key: " + fielda.key() +" value: " + fielda.value());
		        afields.put(fielda.key(), fielda.value());
			}
		}
		
		return afields;
	 
	}

	
}
