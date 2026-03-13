package activeSegmentation;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.SerializedObject;

/**
 * IClassifier defines the core contract for all machine learning classifiers
 * in the Active Segmentation platform.
 *
 * <p>This interface abstracts over Weka-based classifiers (such as Random Forest
 * and Support Vector Machines), allowing the segmentation and classification
 * pipelines to remain independent of any specific classifier implementation.
 * Any Weka-compatible classifier can be plugged in by implementing this interface.</p>
 *
 * <p>The typical lifecycle of a classifier in the pipeline is:</p>
 * <ol>
 *   <li>Build the classifier on a labeled training dataset via {@link #buildClassifier(IDataSet)}</li>
 *   <li>Optionally evaluate performance via {@link #evaluateModel(IDataSet, IFeatureSelection)}</li>
 *   <li>Predict labels for new instances via {@link #classifyInstance(Instance)}</li>
 *   <li>Get probability distributions via {@link #distributionForInstance(Instance)}</li>
 * </ol>
 *
 * <p>Example usage:</p>
 * <pre>
 *     IClassifier classifier = new RandomForestClassifier();
 *     classifier.buildClassifier(trainingData);
 *     double label = classifier.classifyInstance(testInstance);
 *     double[] distribution = classifier.distributionForInstance(testInstance);
 * </pre>
 *
 * @author Sumit Kumar Vohra and Dimiter Prodanov, IMEC
 * @version 1.0
 * @see IDataSet
 * @see IFeatureSelection
 *
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *      Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
public interface IClassifier {

        /**
         * Error code returned when classification fails or produces an invalid result.
         *
         * <p>Callers should check if the return value of {@link #classifyInstance(Instance)}
         * equals this constant to detect classification errors.</p>
         */
        final int ERR_CLASS = -1;

        /**
         * Returns the probability distribution over all classes for the given instance.
         *
         * <p>Each element in the returned array corresponds to the probability that the
         * instance belongs to the class at that index. The values sum to 1.0.
         * This is useful when a soft classification (confidence scores) is needed
         * rather than a hard label.</p>
         *
         * @param instance the {@link Instance} to classify
         * @return a {@code double[]} where each element is the probability for a class;
         *         returns an array of zeros if classification fails
         */
        public double[] distributionForInstance(Instance instance);

        /**
         * Classifies the given instance and returns the predicted class label index.
         *
         * <p>The returned value is the index of the predicted class in the dataset's
         * class attribute. Returns {@link #ERR_CLASS} if classification fails.</p>
         *
         * @param instance the {@link Instance} to classify
         * @return the predicted class label as a {@code double} index,
         *         or {@link #ERR_CLASS} on failure
         * @throws Exception if the classifier encounters an error during prediction
         */
        public double classifyInstance(Instance instance) throws Exception;

        /**
         * Evaluates this classifier against a test dataset and returns a human-readable report.
         *
         * <p>The evaluation report includes metrics such as accuracy, precision, recall,
         * F-measure, and confusion matrix, as produced by Weka's evaluation framework.
         * Feature selection is optionally applied to the test data before evaluation.</p>
         *
         * @param instances the {@link IDataSet} containing labeled test instances
         * @param filter    the {@link IFeatureSelection} filter to apply before evaluation;
         *                  may be {@code null} if no feature selection is needed
         * @return a {@link String} containing the detailed evaluation report
         */
        public String evaluateModel(IDataSet instances, IFeatureSelection filter);

        /**
         * Trains this classifier on the provided labeled dataset.
         *
         * <p>This method must be called before {@link #classifyInstance(Instance)}
         * or {@link #distributionForInstance(Instance)}. All instances in the dataset
         * must have their class attribute set.</p>
         *
         * @param instances the {@link IDataSet} containing labeled training instances
         */
        public void buildClassifier(IDataSet instances);

        /**
         * Trains this classifier on the provided dataset with feature selection applied.
         *
         * <p>Feature selection is performed on the dataset before training, reducing
         * the dimensionality of the feature space. This can improve classifier
         * performance and reduce training time on high-dimensional datasets.</p>
         *
         * @param instances the {@link IDataSet} containing labeled training instances
         * @param selection the {@link IFeatureSelection} filter to apply before training
         */
        public void buildClassifier(IDataSet instances, IFeatureSelection selection);

        /**
         * Sets the underlying Weka {@link Classifier} used by this implementation.
         *
         * <p>This allows swapping out the classifier algorithm at runtime without
         * changing the rest of the pipeline. Common choices include
         * {@code weka.classifiers.trees.RandomForest} and
         * {@code weka.classifiers.functions.SMO} (SVM).</p>
         *
         * @param classifier the Weka {@link Classifier} instance to use
         */
        public void setClassifier(Classifier classifier);

        /**
         * Returns the underlying Weka {@link Classifier} used by this implementation.
         *
         * <p>Useful for inspecting classifier parameters or serializing the
         * trained model for later use.</p>
         *
         * @return the underlying Weka {@link Classifier} instance
         */
        public Classifier getClassifier();

        /**
         * Returns metadata describing this classifier configuration.
         *
         * <p>Metadata typically includes the classifier name, parameter settings,
         * and training dataset information. Used for logging, UI display,
         * and model persistence.</p>
         *
         * @return a {@code String[]} array of metadata key-value pairs
         */
        public String[] getMetadata();

        /**
         * Creates and returns a deep copy of this classifier.
         *
         * <p>Uses Java serialization via {@link SerializedObject} to perform a
         * true deep copy, ensuring the original and copy are completely independent.
         * Returns {@code null} if the copy operation fails.</p>
         *
         * @return a new {@link IClassifier} instance that is a deep copy of this one,
         *         or {@code null} if copying fails
         */
        default IClassifier makeCopy() {
                try {
                        return (IClassifier) new SerializedObject(this).getObject();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return null;
        }
}