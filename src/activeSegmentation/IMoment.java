package activeSegmentation;

import java.util.Set;

public interface IMoment  extends IAnnotated {

	/**
	 * Filter type: segmentation or classification
	 */
	default public FilterType getFilterType() {
		return FilterType.CLASSIF;              //from Enum FilterType
	}
	
	/**
	 * used in for loops  -> typing on method level necessary
	 */
	<T> T getFeatures();
	
	/**
	 * names of features must be unique
	 *Features are implemented as interfaces
	 */

	public Set<String> getFeatureNames();

	
	String getKey();

}
