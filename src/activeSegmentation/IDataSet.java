package activeSegmentation;

import weka.core.Instance;
import weka.core.Instances;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Interface for Dataset required for Learning, 
 * It is generic interface for dataset , User can include weka, Meka dataset 
 * using this interface 
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
public interface IDataSet {
	
	 /**
     * @return The dataset in Weka format
     */
    public Instances getDataset();

    /**
     * @return The number of attributes of the dataset
     */
    public int getNumAttributes();

    /**
     * @return A copy of the current dataset
     */
    public IDataSet copy();

    /**
     * Add to the current dataset the dataset passed as argument
     *
     * @param dataset The dataset to addAll
     */
    public void addAll(IDataSet dataset);

    /**
     * @return The number of instances of the dataset
     */
    public int getNumInstances();

    /**
     * @param index The index of the instance
     * @return The instance
     */
    public Instance instance(int index);

    /**
     * Set the instance in the specified position
     *
     * @param index The index of the position
     * @param instance The instance to set in the position
     */
    public void set(int index, Instance instance);

    /**
     *
     * @return True if the dataset is empty, false otherwise.
     */
    public boolean isEmpty();

    /**
     * Add an instance to the dataset
     *
     * @param instance The instance to add
     */
    public void add(Instance instance);

    /**
     * Remove an instance from dataset
     *
     * @param index The index of the instance to remove
     */
    public void remove(int index);

    /**
     * Removes all elements
     */
    public void delete();

}
