package activeSegmentation;

/*
 * Enumeration for different project types
 */
public enum ProjectType {

	SEGM(1),  
	CLASSIF(2); 
	//SEGM_CLASSIF(3) ;

        //Create New Project dialog box at opening of ActiveSegmentaion PlugIn with option for selection
	
	private final int projectType;

	ProjectType(int projectType) {
		this.projectType = projectType;
	}
        
	public int getProjectType() {
		return this.projectType;
	}
}
