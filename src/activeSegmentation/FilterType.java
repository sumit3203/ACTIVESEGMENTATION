package activeSegmentation;

/*
 * Enumeration for different filter types
 * 	SEGM - segmentation: one to one NxN -> NxN
	CLASSIF - segmentation: many to one NxN -> 1
 */
public enum FilterType {

	SEGM(1),  
	CLASSIF(2),
	NONE(3);
  
	private final int filterType;

	/**
	 * 
	 * @param ft
	 */
	FilterType(int ft) {
		this.filterType = ft;
	}

	/**
	 * 
	 * @return
	 */
	public int getFilterType() {
		return filterType;
	}
}