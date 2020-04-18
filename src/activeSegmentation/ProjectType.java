package activeSegmentation;

/*
 * Enumeration for different project types
 */
public enum ProjectType {

	SEGM(1),  
	CLASSIF(2); 
	//SEGM_CLASSIF(3) ;


	private final int projectType;

	ProjectType(int projectType) {
		this.projectType = projectType;
	}

	public int getProjectType() {
		return this.projectType;
	}
}
