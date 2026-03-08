package activeSegmentation;


import java.util.Map;
import ij.gui.Roi;
import ij.process.ImageProcessor;


/**
 * IMoment defines the contract for moment-based feature extractors in the Active Segmentation platform.
 *
 * <p>Moments are mathematical descriptors that capture the statistical and geometric properties
 * of pixel intensity distributions within an image or region of interest. They extend the
 * {@link IFilter} interface and are primarily used in the <strong>classification</strong> pipeline,
 * as opposed to pixel-level segmentation.</p>
 *
 * <p>Implementations of this interface compute a set of named scalar features from an image
 * region (ROI), which are then assembled into a feature vector for machine learning classifiers
 * such as Random Forest or SVM (via Weka).</p>
 *
 * <p>Common moment types include:</p>
 * <ul>
 *   <li>Statistical moments (mean, variance, skewness, kurtosis)</li>
 *   <li>Hu moments (rotation-invariant geometric descriptors)</li>
 *   <li>Zernike moments (orthogonal polynomial-based descriptors)</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 *     IMoment&lt;double[]&gt; moment = new HuMoment();
 *     Pair&lt;String, double[]&gt; result = moment.apply(imageProcessor, roi);
 *     Set&lt;String&gt; featureNames = moment.getFeatureNames();
 * </pre>
 *
 * @param <T> the type of the feature container returned by {@link #getFeatures()},
 *            typically {@code double[]} or a {@code Map}
 *
 * @author Sumit Kumar Vohra, ZIB and Dimiter Prodanov, IMEC
 * @version 1.0
 * @see IFilter
 * @see FilterType
 */
public interface IMoment<T> extends IAnnotated, IFilter {

        /**
         * Returns the filter type for moment-based features.
         *
         * <p>Moments are used exclusively in the classification pipeline,
         * so this always returns {@link FilterType#CLASSIF}.</p>
         *
         * @return {@link FilterType#CLASSIF} indicating classification use
         */
        @Override
        default public FilterType getFilterType() {
                return FilterType.CLASSIF;
        }

        /**
         * Returns the computed features for this moment.
         *
         * <p>The return type {@code T} is implementation-specific commonly a
         * {@code double[]} array or a {@link Map} of feature names to values.
         * This method is typically called after {@link #apply(ImageProcessor, Roi)}
         * has been executed.</p>
         *
         * <p>Note: This method is designed for use in for-loops; typing is
         * intentionally defined at the method level for flexibility.</p>
         *
         * @return the computed feature container of type {@code T}
         */
        public T getFeatures();

        /**
         * Returns the set of unique feature names produced by this moment.
         *
         * <p>Feature names must be globally unique across all moments u
        */
}