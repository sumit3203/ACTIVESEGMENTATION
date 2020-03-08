package activeSegmentation;


import java.util.Set;

import weka.core.Instance;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Manager for learning. 
 * 
 * to be changed into an abstract class
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


public interface ILearningManager {

	/**
	 * This method will set the particular type of classifier for learing like 
	 * Support Vector Machines, Deep Learning etc
	 * given by key
	 * @param classifier 
	 */
	public void setClassifier(Object classifier);

	public Object getClassifier();
	/**
	 * This method will train the classifier 
	 * 
	 */
	public void trainClassifier();

	/**
	 * This method will save the training metadata using MetaInfo
	 * 
	 */
	public void saveLearningMetaData();

	/**
	 * This method will load the training metadata using MetaInfo
	 * 
	 */
	public void loadLearningMetaData();

	/**
	 * This method apply Classifier on test DataSet
	 * @param testDataSet
	 * @return Predicted class of test DataSet
	 */
	public  double[] applyClassifier(IDataSet testDataSet);
	
	public double predict(Instance instance);

	public Set<String> getFeatureSelList();
}
