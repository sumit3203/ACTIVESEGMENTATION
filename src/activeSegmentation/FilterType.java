package activeSegmentation;

/*
 * Enumeration for different filter types
 * 	SEGM - segmentation: one to one NxN -> NxN
	CLASSIF - segmentation: many to one NxN -> 1
	FEATURE - for feature selection
 */
public enum FilterType {

	SEGM(1),  
	CLASSIF(2),
	FEATURE(3),
	NONE(4);
  
	private final int filterType;

	/**
	 * 
	 * @param ft
	 */
	FilterType(int ft) {
		filterType = ft;
	}

	/**
	 * 
	 * @return
	 */
	public int getFilterType() {
		return filterType;
	}
}