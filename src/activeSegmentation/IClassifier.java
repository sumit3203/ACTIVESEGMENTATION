package activeSegmentation;


//import activeSegmentation.prj.ProjectInfo;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.SerializedObject;

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
 * general use: classification + segmentation 
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
	
	final int ERR_CLASS=-1;
	
	/*
     * It builds the classifier on the instances.
     * based on WEKA -> WekaClassifier
     */
	
    /**
     * @param instance The instance
     * @return the distribution for instance
     */
    public double[] distributionForInstance(Instance instance);
    
    /**
    *
    * @param instance The specific instance to classify.
    * @return The predicted label for the classifier.
    * @throws Exception The exception that will be launched.
    */
    public double classifyInstance(Instance instance) throws Exception ;

    //////////////////////////////////
    // potentially mixed code
    ////////////////////////////////
	
	 /**
     * Evaluates the classifier using the test dataset and stores the evaluation.
     * Tests
     * @param instances The instances to test
     * @return Detailed human-readable output of the classifier
     */
 
     public String evaluateModel(IDataSet instances);
    
 	/**
      * @param instances The data provided to classify
      * @throws Exception The exception that will be launched.
      */
     public void buildClassifier(IDataSet instances);
     
     public void buildClassifier(IDataSet instances,  IFeatureSelection selection);
     
     //////////////////////////////////
     // Non Weka-specific code
     ////////////////////////////////
     
     /**
      * Sets classifier
      * @param classifier
      */
  	public  void setClassifier(Classifier classifier);

  //	public void select
  	
    /**
     * @return The copy of the IClassifier used.
     * @throws Exception The exception that will be launched.
     */
	default IClassifier makeCopy() {
		try {
			return (IClassifier) new SerializedObject(this).getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    

    public Classifier getClassifier();
    
    
    public String[] getMetadata();

	

    
    
   // public ProjectInfo getMetaInfo() ;

	//public void setMetaInfo(ProjectInfo metaInfo);


}
