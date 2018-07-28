package activeSegmentation;

public enum ProjectType {

	SEGMENTATION(1),  
	CLASSIFICATION(2), 
	BOTH   (3) ;


	private final int projectType;

	ProjectType(int projectType) {
		this.projectType = projectType;
	}

	public int getProjectType() {
		return this.projectType;
	}
}
