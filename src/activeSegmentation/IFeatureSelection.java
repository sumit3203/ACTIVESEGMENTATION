package activeSegmentation;

import java.util.*;

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

	
	public IDataSet selectFeatures(IDataSet data);
	
	
	public IDataSet filterData(IDataSet data);
	
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
	
	/**
	 * 
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param order
	 * @return
	 */
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> sortByVal(Map<K,V> map, int order) {
		SortedSet<Map.Entry<K,V>> sortedentries =new TreeSet<> (
				new Comparator<Map.Entry<K,V>>() {
					@Override
					public int compare( Map.Entry<K,V> e1, Map.Entry<K,V> e2 ) {
						return (order>0)? compareToReturnDuplicates(e1.getValue(), e2.getValue()): compareToReturnDuplicates(e2.getValue(), e1.getValue()) ;
					}
					
				}
				
				);
		sortedentries.addAll(map.entrySet());
		return sortedentries;
				
	}
	
	
	/**
	 * 
	 * @param <V>
	 * @param v1
	 * @param v2
	 * @return
	 */
	static <V extends Comparable<? super V>> int compareToReturnDuplicates(V v1, V v2) {
		return (v1.compareTo(v2)==-1)?-1:1;
	}
	
 
}
