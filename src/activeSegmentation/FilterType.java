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
	FilterType(int ft) {
		this.filterType = ft;
	}
        //user input Filtertype at the starting dialog box of ActiveSegmentation Filter dialog box 
	public int getFilterType() {
		return filterType;
	}
}
