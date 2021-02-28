package activeSegmentation;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Interface for Filter manager, It is responsible of doing all the Saving, loading
 * the filter
 * 
 *  stays as an interface  
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
public interface IFilterManager {

	/**
	 * This method will load list of filter from particular
	 * directory, It can load filters from jar file 
	 * @param diretory of filter
	 *
	 */
	public Map<String, Map<String,String>> annotationMap= new HashMap<>();

	public  void loadFilters(List<String> home)throws InstantiationException, IllegalAccessException, 
	IOException, ClassNotFoundException;
	
	/**
	 * This method will apply list of filters on particular 
	 * image 
	 * @param image on which filter is applied
	 *
	 */
	public void applyFilters();
	
	/**
	 * This method will set of filters loaded by the plugin  
	 * 
	 * @return set of loaded or available filters
	 *
	 */
	public Set<String> getAllFilters();
	
	/**
	 * This method will give the setting of the particular filters 
	 * 
	 * @param key of the filter
	 *
	 */
	public Map<String,String> getDefaultFilterSettings(String key);
	
	
	public IFilter getInstance(String key);
	
	/**
	 * This method will update the setting of the particular filters 
	 * given by key
	 * @param key of the filter
	 *@param  updated setting map
	 */
	public boolean updateFilterSettings(String key, Map<String,String> settingsMap);
	
	 	
	/**
	 * This method will return processed image by particular filter
	 * @param  filter key
	 * @return extracted Image
	 */
	public Image getFilterImage(String key);
	
	/**
	 * This method will change filter settings to default
	 * @param  filter key
	 * @return success flag
	 */
	public boolean setDefault(String key);
	
	/**
	 * This method is to check whether filter is enabled or not
	 * @param  filter key
	 * @return success flag
	 */
	public boolean isFilterEnabled(String key);
	
	/**
	 * This method  enables the filter
	 * @param  filter key
	 */
	public void enableFilter(String key);
	
	
	/**
	 * sets filters Meta Data using MetaInfo
	 */
	public void setFiltersMetaData();
	
	/**
	 * 
	 *   saves filters Meta data using MetaInfo
	 */
	public void saveFiltersMetaData();
	
	
	
	
	public static Map<String, String> getFieldAnnotations(String key) {
		return annotationMap.get(key);
	}


}
