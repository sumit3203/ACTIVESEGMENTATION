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
package activeSegmentation.learning;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;





import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
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
	public void buildClassifier(IDataSet instances) throws Exception {
		classifier.buildClassifier(instances.getDataset());
	}

	/**
	 *
	 * @param instance The instance to classify.
	 * @return The predicted label for the classifier.
	 * @throws Exception The exception that will be launched.
	 */
	@Override
	public double classifyInstance(Instance instance) throws Exception {
		return classifier.classifyInstance(instance);
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
	 * @param classifier The weka classifier.
	 */
	public void setClassifier(Classifier classifier) {
		try {
			this.classifier = weka.classifiers.AbstractClassifier
					.makeCopy(classifier);
		} catch (Exception e) {
			Logger.getLogger(WekaClassifier.class.getName()).log(
					Level.SEVERE, null, e);
		}
	}


	/**
	 * Evaluates the classifier using the test dataset and stores the evaluation.
	 *
	 * @param instances The instances to test
	 * @return The evaluation
	 */
	@Override
	public double[] testModel(IDataSet instances) {

		try {

			// test the current classifier with the test set
			Evaluation evaluator = new Evaluation(new Instances(instances.getDataset(), 0));

			
			double[] predict =evaluator.evaluateModel(classifier, instances.getDataset());

			//System.out.println(evaluator.toSummaryString());
			return predict;


		} catch (Exception e) {
			Logger.getLogger(WekaClassifier.class.getName()).log(
					Level.SEVERE, null, e);
		}

		return null;
	}


	@Override
	public IClassifier makeCopy() throws Exception {
		// TODO Auto-generated method stub
		return (IClassifier) new SerializedObject(this).getObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Classifier getClassifier() {
		// TODO Auto-generated method stub
		return this.classifier;
	}

}
