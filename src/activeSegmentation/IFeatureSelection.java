package activeSegmentation;

import ijaux.datatype.Pair;
import weka.core.SerializedObject;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents used for feature selection
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

public interface IFeatureSelection extends IAnnotated {

	
	public IDataSet selectFeatures(IDataSet trainingData);
	
	
	public IDataSet applyOnTestData(IDataSet data);
	
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
     * @return The copy of the IClassifier used.
     * @throws Exception The exception that will be launched.
     */
	default IFeatureSelection makeCopy() {
		try {
			return (IFeatureSelection) new SerializedObject(this).getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
 
}
