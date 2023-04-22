package activeSegmentation;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;

public interface IMoment<T>  extends IAnnotated, IFilter {

	/**
	 * Filter type: segmentation or classification
	 */
	@Override
	default public FilterType getFilterType() {
		return FilterType.CLASSIF;
	}
	
	/**
	 * used in for loops  -> typing on method level necessary
	 */
	T getFeatures();
	
	/**
	 * names of features must be unique
	 */
	public Set<String> getFeatureNames();
	
	public Pair<String, double[]> apply(ImageProcessor imageProcessor, Roi roi);
	
	/**
	 * Provides filter-specific help message/ resource 
	 * using the annotation mechanism
	 * @return String
	 */
	@Override
	default public String helpInfo() {
		return "This is a moment";
	}
	
	@Override
	public default  Map<String, String> getDefaultSettings() {
		return new HashMap<String, String>();
	}
}