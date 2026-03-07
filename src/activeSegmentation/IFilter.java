package activeSegmentation;

import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * IFilter defines the core contract for all image filters in the Active Segmentation platform.
 *
 * <p>Filters are the fundamental building blocks of the segmentation pipeline. Each filter
 * extracts specific features from an image (e.g., Gaussian blur, edge detection, curvature)
 * that are subsequently used by machine learning classifiers to perform pixel-level
 * segmentation or whole-image classification.</p>
 *
 * <p>This interface supports both segmentation and classification workflows. Implementing
 * classes must provide feature extraction logic via {@link #applyFilter}, along with
 * settings management via {@link #getDefaultSettings} and {@link #updateSettings}.</p>
 *
 * <p>Filters are extensible via the plugin mechanism — new filters are loaded automatically
 * from the plugin path at startup.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     IFilter filter = new GaussianFilter();
 *     filter.updateSettings(filter.getDefaultSettings());
 *     filter.applyFilter(imageProcessor, outputPath, roiList);
 * </pre>
 *
 * @author Sumit Kumar Vohra, ZIB and Dimiter Prodanov, IMEC
 * @version 1.0
 * @see IMoment
 * @see FilterType
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
public interface IFilter extends IAnnotated {

        /** Platform-independent file separator for constructing file paths. */
        public static final String fs = File.separator;

        /**
         * Returns a new default settings map for the filter.
         *
         * <p>The map contains parameter names as keys and their default values as strings,
         * following the format used by the {@code ij.Prefs} class in ImageJ.</p>
         *
         * @return a {@link Map} of parameter names to their default string values
         */
        public Map<String, String> getDefaultSettings();

        /**
         * Updates the filter settings with the provided values.
         *
         * <p>Each entry in the map represents a parameter name and its new value.
         * Implementations should validate the values before applying them.</p>
         *
         * @param settingsMap a {@link Map} of parameter names to their new string values
         * @return {@code true} if settings were updated successfully, {@code false} otherwise
         */
        public boolean updateSettings(Map<String, String> settingsMap);

        /**
         * Applies this filter to the given image and writes the output to the specified path.
         *
         * <p>When a non-empty {@code roiList} is provided, the filter operates only within
         * those regions of interest rather than on the full image, improving performance
         * for large images.</p>
         *
         * @param image     the {@link ImageProcessor} containing the pixel data of the 2D image
         * @param path      the folder path where the filter output will be stored
         * @param roiList   a {@link List} of {@link Roi} regions to restrict processing;
         *                  pass an empty list to process the entire image
         */
        public void applyFilter(ImageProcessor image, String path, List<Roi> roiList);

        /**
         * Returns the unique key identifying this filter.
         *
         * <p>The key is the first element of the key-value pair returned by
         * {@link IAnnotated#getKeyVal()}. It is used internally to look up and
         * reference filters in maps and databases.</p>
         *
         * @return a {@link String} containing the unique filter key
         */
        public default String getKey() {
                Pair<String, String> p = getKeyVal();
                return p.first;
        }

        /**
         * Returns the human-readable display name of this filter.
         *
         * <p>The name is the second element of the key-value pair returned by
         * {@link IAnnotated#getKeyVal()}. It is shown in the UI filter selection panels.</p>
         *
         * @return a {@link String} containing the display name of the filter
         */
        public default String getName() {
                Pair<String, String> p = getKeyVal();
                return p.second;
        }

        /**
         * Returns whether this filter is currently enabled.
         *
         * <p>Disabled filters are skipped during the feature extraction pipeline,
         * allowing users to selectively include or exclude specific features.</p>
         *
         * @return {@code true} if the filter is enabled, {@code false} if disabled
         */
        public boolean isEnabled();

        /**
         * Resets all filter settings back to their default values.
         *
         * <p>Equivalent to calling {@link #updateSettings(Map)} with the result of
         * {@link #getDefaultSettings()}.</p>
         *
         * @return {@code true} if settings were successfully reset, {@code false} otherwise
         */
        public boolean reset();

        /**
         * Enables or disables this filter in the processing pipeline.
         *
         * <p>When set to {@code false}, the filter will be skipped during feature
         * extraction. This is useful for experimenting with different feature subsets
         * without removing filters entirely.</p>
         *
         * @param isEnabled {@code true} to enable the filter, {@code false} to disable it
         */
        public void setEnabled(boolean isEnabled);

        /**
         * Returns the type of this filter as a {@link FilterType} enum value.
         *
         * <p>The default implementation returns {@link FilterType#SEGM}, indicating
         * this filter is used for segmentation. Override to return
         * {@link FilterType#CLASSIF} for classification-only filters.</p>
         *
         * @return the {@link FilterType} of this filter
         */
        default public FilterType getFilterType() {
                return FilterType.SEGM;
        }

        /**
         * Returns a help message or documentation string for this filter.
         *
         * <p>This is displayed in the UI help panel when the user selects the filter.
         * Override this method to provide filter-specific guidance to the user.</p>
         *
         * @return a {@link String} containing the help text for this filter
         */
        default public String helpInfo() {
                return "This is a filter";
        }

        /**
         * Returns the version string of this filter implementation.
         *
         * <p>Used for tracking filter versions across releases. Override to return
         * the appropriate version for custom filter implementations.</p>
         *
         * @return a {@link String} representing the filter version, e.g. {@code "1.0"}
         */
        default public String getVersion() {
                return "1.0";
        }
}