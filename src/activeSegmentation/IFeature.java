package activeSegmentation;


import java.util.ArrayList;
import java.util.List;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Interface for feature type like segmentation, classification or
 * objection Detection. This is also in development stage
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
public interface IFeature {
	
	public String getFeatureName();
	
	/**
	 * Create Instances of Training DataSet
	 * @param classLabels
	 * @param classes
	 * @param features can be of pixel level features or class level features
	 */
	public void createTrainingInstance(List<String> classLabels,
			int classes, List<?> features);
	/**
	 * 
	 * @return Weka Format DataSet 
	 */
	public IDataSet getDataSet();
	
	/**
	 * 
	 * @param trainingData
	 */
	public void setDataset(IDataSet trainingData);
	
	/**
	 * Create Instances of Testing DataSet at Pixel level
	 * @param classLabels
	 * @param classes
	 */
	public List<IDataSet> createAllInstance(List<String> classLabels, int classes);
	
	/**
	 * Create Instances of Testing DataSet at Class level
	 * @param classLabels
	 * @param classes
	 * @param testimageindex contains indexes of all images present in a particular class
	 */
	public List<IDataSet> createAllInstance(List<String> classLabels, int classes,List<ArrayList<Integer>> testimageindex);

}
