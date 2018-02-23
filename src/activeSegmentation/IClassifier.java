package activeSegmentation;


import weka.classifiers.Classifier;
import weka.core.Instance;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Interface for classifier, It will allow to use any 
 * type of classifier in our system
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
public interface IClassifier {
	
	/**
     * It builds the classifier on the instances.
     *
     * @param instances The instances to use
     * @throws Exception The exception that will be launched.
     */
    public void buildClassifier(IDataSet instances) throws Exception;

    /**
     * @param instance The instance
     * @return the distribution for instance
     */
    public double[] distributionForInstance(Instance instance);
    
    /**
    *
    * @param instance The instance to classify.
    * @return The predicted label for the classifier.
    * @throws Exception The exception that will be launched.
    */
    public double classifyInstance(Instance instance) throws Exception;

    /**
    *
    * @param classifier
    */
	public void setClassifier(Classifier classifier);

	
	 /**
     * Evaluates the classifier using the test dataset and stores the evaluation.
     *
     * @param instances The instances to test
     * @return The evaluation
     */
 
     public double[] testModel(IDataSet instances);
    
    /**
     * @return The copy of the IClassifier used.
     * @throws Exception The exception that will be launched.
     */
    public IClassifier makeCopy() throws Exception;


}
