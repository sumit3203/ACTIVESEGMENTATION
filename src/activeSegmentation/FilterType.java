package activeSegmentation;

public enum FilterType {

	SEGM(1),  
	CLASSIF(2);
  
	private final int filterType;

	FilterType(int ft) {
		this.filterType = ft;
	}

	public int getFilterType() {
		return filterType;
	}
}