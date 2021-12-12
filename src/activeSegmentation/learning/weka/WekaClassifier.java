/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package activeSegmentation.learning.weka;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
//import activeSegmentation.prj.ProjectInfo;
//import activeSegmentation.util.InstanceUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializedObject;

public class WekaClassifier implements IClassifier, Serializable {

	private static final long serialVersionUID = 1765269739169476036L;

	/**
	 * Classifier from Weka.
	 */
	private Classifier classifier;

	
	public WekaClassifier(Classifier iClassifier) {
	   this.classifier= iClassifier;
	}

	/**
	 * Constructs the learning model from the dataset.
	 *
	 * @param instances The instances to train the classifier
	 * @throws Exception The exception that will be launched.
	 */
	@Override
	public void buildClassifier(IDataSet instances) {
		try {
			classifier.buildClassifier(instances.getDataset());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param instance The instance to classify.
	 * @return The predicted label for the classifier.
	 * @throws Exception The exception that will be launched.
	 */
 
	@Override
	public double classifyInstance(Instance instance)  {
		try {
			return classifier.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ERR_CLASS;
	}

	/**
	 * Returns the probability that has the instance to belong to each class.
	 * every instance of belonging to every class that the dataset contains.
	 *
	 * @param instance The instance to test.
	 * @return The probabilities for each class
	 */
 
	@Override
	public double[] distributionForInstance(Instance instance) {

		try {
			return classifier.distributionForInstance(instance);
		} catch (Exception e) {
			Logger.getLogger(WekaClassifier.class.getName()).log(
					Level.SEVERE, null, e);
		}
		return null;
	}

	/**
	 * The simple name of the class.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return classifier.getClass().getSimpleName();
	}

	/**
	 * Set the classifier to use.
	 *
	 * @param classifier The WEKA classifier.
	 */
	 
	@Override
	public void setClassifier(Classifier classifier) {
		try {
			this.classifier = AbstractClassifier.makeCopy(classifier);
		} catch (Exception e) {
			Logger.getLogger(WekaClassifier.class.getName()).log(
					Level.SEVERE, null, e);
		}
	}


	/**
	 * Evaluates the classifier using the test dataset and stores the evaluation.
	 *
	 * @param iData The instances to test
	 * @return The evaluation
	 */
	@Override
	public String evaluateModel(IDataSet iData) {

		try {

			// test the current classifier with the test set
			//Instances wekadata=new Instances(instances.getDataset(),0);
			Instances wekainst=iData.getDataset();
			Evaluation evaluator = new Evaluation(wekainst);
			
			evaluator.evaluateModel(classifier, wekainst);
			 
			// 10 fold cross-validation
			//evaluator.crossValidateModel(classifier, wekadata, 10,  new Random(1));
			String outputstr=evaluator.toSummaryString("\n\nSummary Results\n======\n", false);
			//System.out.println(outputstr);
			outputstr+=evaluator.toClassDetailsString("\nDetailed Results\n======\n");
			//System.out.println(outputstr);
			
			return outputstr;


		} catch (Exception e) {
			Logger.getLogger(WekaClassifier.class.getName()).log(
					Level.SEVERE, null, e);
		}

		return null;
	}


	@Override
	public IClassifier makeCopy() {
		try {
			return (IClassifier) new SerializedObject(this).getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

 
	@Override
	public Classifier getClassifier() {
		return this.classifier;
	}

	@Override
	public String[] getMetadata() {
		AbstractClassifier cls = (AbstractClassifier) classifier;
		return cls.getOptions();
	}

 
}
