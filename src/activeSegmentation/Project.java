package activeSegmentation;

import ij.ImagePlus;

public class Project {

	private String projectName;
	private String projectDirectory;
	private String projectDescription;	
	private ImagePlus image;
	
	
	
	public ImagePlus getImage() {
		return image;
	}
	public void setImage(ImagePlus image) {
		this.image = image;
	}
	public Project(ImagePlus image,String projectName, String projectDirectory, String projectDescription) {
		super();
		this.image= image;
		this.projectName = projectName;
		this.projectDirectory = projectDirectory;
		this.projectDescription = projectDescription;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getProjectDirectory() {
		return projectDirectory;
	}
	public void setProjectDirectory(String projectDirectory) {
		this.projectDirectory = projectDirectory;
	}
	public String getProjectDescription() {
		return projectDescription;
	}
	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}
	
	
}
