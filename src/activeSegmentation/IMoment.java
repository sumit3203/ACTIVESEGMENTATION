package activeSegmentation;


import java.util.Set;

public interface IMoment<T>  extends IAnnotated, IFilter {

	/**
	 * Filter type: segmentation or classification
	 */
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
	
	
	
	

}