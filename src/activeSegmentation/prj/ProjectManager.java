package activeSegmentation.prj;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import activeSegmentation.ASCommon;
import static activeSegmentation.ASCommon.*;
import activeSegmentation.IDataSet;
import activeSegmentation.IMoment;
import activeSegmentation.IUtil;
import activeSegmentation.ProjectType;

public class ProjectManager implements IUtil{

	private IDataSet dataSet;
	private static ProjectInfo projectInfo;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private String activeSegJarPath;
	private Map<String,String> projectDir=new HashMap<>();
	private Map<String, IMoment<?>> computedMomentMap= new HashMap<>();

	/**
	 * 
	 * @return
	 */
	public IDataSet getDataSet() {
		return  dataSet;
	}

	/**
	 * 
	 * @param data
	 */
	public void setData(IDataSet data) {
		dataSet = data.copy();
	}
	

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean loadProject(String fileName) {
		//IJ.log(System.getProperty("plugins.dir"));
		IJ.log("loading project ...");
		setASpath();
		if(projectInfo==null){
			ObjectMapper mapper = new ObjectMapper();
			try {
				//System.out.println(fileName);
				File projectFile=new File(fileName);
				// elementary check if the file is a project file
				if (projectFile.getName().indexOf(".json") <0) return false;
				
				projectInfo= mapper.readValue(projectFile, ProjectInfo.class);
				
				String loadedVersion = projectInfo.getVersion();
				if (!loadedVersion.equals(ProjectInfo.compatibleVersion)) {
					performVersionMigration(loadedVersion);
				}

				System.out.println("loading learning object");
				System.out.println(projectInfo.getLearning());
		 
				
				String loadedVersion1 = projectInfo.getVersion();
				if (!loadedVersion1.equals(ProjectInfo.compatibleVersion)) {
					performVersionMigration(loadedVersion1);
					// we will decide what to do later
					IJ.error(loadedVersion1+ " is incompatible with " + ProjectInfo.compatibleVersion);
				}
					 
				
				setProjectDir(projectFile.getParent(), null);
				projectInfo.setProjectDirectory(projectDir);
				//System.out.println(projectInfo.toString());
				IJ.log("project type "+projectInfo.getProjectType() );
				IJ.log("....");
			} catch (UnrecognizedPropertyException e) {
				IJ.log("Error: Wrong version");
				e.printStackTrace();
				return false;
			} catch (JsonGenerationException e) {
				IJ.log("Error: Not a JSON file!");
				e.printStackTrace();
				return false;
			} catch (JsonMappingException e) {
				IJ.log("Error: Wrong version mapping");
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				IJ.log("Error: IO");
				e.printStackTrace();
				return false;
			}			


		}

		return true;
	}
	
	

	public void performVersionMigration(String loadedVersion) {
        if (!ProjectInfo.compatibleVersion.equals(loadedVersion)) {
            // Perform migration steps based on version changes
            if (loadedVersion.compareTo(ProjectInfo.compatibleVersion) < 0) {
                // Initialize a new field or load external resources to add
                // newField = "default_value";
            }
            // Add more migration steps for other versions as needed
            // Update the version to the current compatible version
            projectInfo.setVersion(ProjectInfo.compatibleVersion);
			System.out.println("version updated to " + ProjectInfo.compatibleVersion);
        }
    }
	 
	
	/**
	 * 
	 * @param project
	 */
	public void writeMetaInfo( ProjectInfo project) {
		updateMetaInfo(project);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			projectInfo.setModifyDate(dateFormat.format(new Date()));
			if(projectInfo.getCreatedDate()==null){
				projectInfo.setCreatedDate(dateFormat.format(new Date()));
			}
		
			final String projectFile=projectInfo.getProjectPath()+fs+
					 projectInfo.projectName+".json";
			mapper.writeValue(new File(projectFile), projectInfo);

			System.out.println("ProjectManager: saving project file "+projectFile);
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public ProjectInfo getMetaInfo() {
		return projectInfo;
	}

	/**
	 * 
	 * @param projectName
	 * @param projectType
	 * @param projectDirectory+ 
	 * @param projectDescription
	 * @param trainingImage
	 * @return
	 */
	public String createProject(String projectName, String projectType,String projectDirectory, 
			String projectDescription,
			String trainingImage){
		String message="done";
		setASpath();
		projectInfo= new ProjectInfo();
		projectInfo.setProjectPath(projectDirectory+ fs+projectName);
	
		projectInfo.projectName=projectName;
		projectInfo.setProjectType(ProjectType.valueOf(projectType));

		projectInfo.projectDescription=projectDescription;
		List<String> jars= new ArrayList<>();
		jars.add(activeSegJarPath);
		projectInfo.setPluginPath(jars);
		//DEFAULT 2 classes
		projectInfo.setNClasses(2);
		createProjectSpace(projectDirectory, projectName);
		//CURRENT IMAGE
		if (null !=WindowManager.getCurrentImage()) {
			ImagePlus image= WindowManager.getCurrentImage();
			IJ.log(Integer.toString(image.getStackSize()));
			IJ.log(image.getTitle());
            createImages(image.getTitle(), image);
		}else { 
			
			if(trainingImage.endsWith(".tif")|| trainingImage.endsWith(".tiff") || trainingImage.endsWith(".jpg")) {
				ImagePlus currentImage=IJ.openImage(trainingImage);
				createImages(currentImage.getTitle(), currentImage);
			
			}else {
				// TRAINING IMAGE FOLDER
				List<String> images=loadImagesSubDirectoryPath(trainingImage, true);
				for(String image: images) {
					ImagePlus currentImage=IJ.openImage(trainingImage+fs+image);
					createImages(image.substring(image.lastIndexOf("\\") + 1), currentImage);
				}
			}
			
			
		}

		projectInfo.setProjectDirectory(projectDir);
		writeMetaInfo(projectInfo);
		return message;
	}

	/**
	 * 
	 * @param image
	 * @param currentImage
	 */
	private void createImages(String image, ImagePlus currentImage) {
		
		final int dotindex=image.lastIndexOf(".");
		String format="tif";
		String folder=image;
		if (dotindex>0) {
		  format=image.substring(dotindex);
		  folder=image.substring(0, dotindex);	
		}
		if(currentImage.getStackSize()>0) {
			createStackImage(currentImage,format,folder);
		}else {
			createDirectory(projectDir.get(ASCommon.K_FILTERSDIR)+folder);
			IJ.saveAs(currentImage,format,projectDir.get(ASCommon.K_IMAGESDIR)+folder);

		}

	}
	
	/**
	 * 
	 * @param image
	 * @param format
	 * @param folder
	 */
	private void createStackImage(ImagePlus image,String format, String folder) {
		IJ.log("createStack");
		//String format=image.getTitle().substring(image.getTitle().lastIndexOf("."));
		//String folder=image.getTitle().substring(0, image.getTitle().lastIndexOf("."));
		IJ.log(format);
		for(int i=1; i<=image.getStackSize();i++) {
			ImageProcessor processor= image.getStack().getProcessor(i);
			String titleFullPath= folder+i;
			IJ.log(folder);
			IJ.log(titleFullPath);
			String imageTitle = titleFullPath.substring(titleFullPath.lastIndexOf('\\') + 1);
			createDirectory(projectDir.get(ASCommon.K_FILTERSDIR)+imageTitle);
			IJ.saveAs(new ImagePlus(titleFullPath, processor),format,projectDir.get(ASCommon.K_IMAGESDIR)+imageTitle);
		}
		IJ.log("createStack done");
	}
	
	/**
	 * 
	 * @return computedMomentMap
	 */
    public Map<String, IMoment<?>> getComputedMomentMap() {
        return computedMomentMap;
    }

    /**
	 * 
	 * @param computedMomentMap
	 */
    public void setComputedMomentMap(Map<String, IMoment<?>> computedMomentMap) {
        this.computedMomentMap = computedMomentMap;
    }

	/**
	 * 
	 */
	private void setASpath() {
		String plugindir=IJ.getDir("imagej");
		IJ.log(plugindir);
		// add an option for main plugin folder + check for IJ property
		String aspath=(String) IJ.getProperty("AS_pluginpath");
		System.out.println("aspath "+aspath);
		if (aspath==null) {
			aspath= "activeSegmentation";
		}
		
		activeSegJarPath=plugindir+"plugins"+fs+"activeSegmentation"+fs+"ACTIVE_SEG.jar";
		// activeSegJarPath = "C:\\Users\\aarya\\Downloads\\ImageJ\\plugins\\activeSegmentation\\ACTIVE_SEG.jar";
		IJ.log(activeSegJarPath);
		System.out.println("activeSegPath=" + activeSegJarPath);
		//System.out.println(System.getProperty("plugins.dir"));
		IJ.log("plugins.dir: "+System.getProperty("plugins.dir"));
	}

	/**
	 * 
	 * @param projectDirectory
	 * @param projectName
	 */
	private void setProjectDir(String projectDirectory, String projectName) {
		String projectString;
		if(projectName!=null) {
			projectString=projectDirectory+fs+projectName+offsetDir;
		}else {
			projectString=projectDirectory+offsetDir;
		}
		
		projectDir.put(ASCommon.K_PROJECTDIR,   projectString);
		projectDir.put(ASCommon.K_FILTERSDIR,   projectString + ASCommon.filterDir);
		projectDir.put(ASCommon.K_FEATURESDIR,  projectString + ASCommon.featureDir);
		projectDir.put(ASCommon.K_LEARNINGDIR,  projectString + ASCommon.learnDir);
		projectDir.put(ASCommon.K_EVALUATIONDIR,projectString + ASCommon.evalDir);
		projectDir.put(ASCommon.K_IMAGESDIR,    projectString + ASCommon.imagDir);
		projectDir.put(ASCommon.K_TESTIMAGESDIR,projectString + ASCommon.testimagDir);
		projectDir.put(ASCommon.K_TESTFILTERDIR,projectString + ASCommon.testfilterDir);
		
	}
	
	private void createProjectSpace(String projectDirectory, String projectName) {

		setProjectDir(projectDirectory, projectName);
		createDirectory(projectDir.get(ASCommon.K_PROJECTDIR));
		createDirectory(projectDir.get(ASCommon.K_FILTERSDIR));
		createDirectory(projectDir.get(ASCommon.K_FEATURESDIR));
		createDirectory(projectDir.get(ASCommon.K_LEARNINGDIR));
		createDirectory(projectDir.get(ASCommon.K_EVALUATIONDIR));
		createDirectory(projectDir.get(ASCommon.K_IMAGESDIR));
		createDirectory(projectDir.get(ASCommon.K_TESTIMAGESDIR));
		createDirectory(projectDir.get(ASCommon.K_TESTFILTERDIR));
		IJ.log("Project folders created");
	}
/*	
	private File[] sortFiles(File[] images) {
		final Pattern p = Pattern.compile("\\d+");
		Arrays.sort(images, new  Comparator<File>(){
			@Override public int compare(File o1, File o2) {
				Matcher m = p.matcher(o1.getName());
				Integer number1 = null;
				if (!m.find()) {
					return o1.getName().compareTo(o2.getName());
				}
				else {
					Integer number2 = null;
					number1 = Integer.parseInt(m.group());
					m = p.matcher(o2.getName());
					if (!m.find()) {
						return o1.getName().compareTo(o2.getName());
					}
					else {
						number2 = Integer.parseInt(m.group());
						int comparison = number1.compareTo(number2);
						if (comparison != 0) {
							return comparison;
						}
						else {
							return o1.getName().compareTo(o2.getName());
						}
					}
				}
			}}
				);
		return images;
	}
	*/
/*
	@Override
	public List<String> loadImages(String directory){
		List<String> imageList= new ArrayList<>();
		File folder = new File(directory);
		File[] images = sortFiles(folder.listFiles());
		
		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}
		
		return imageList;
	}
*/
	
	private boolean createDirectory(String project){
		File file=new File(project);
		if(!file.exists()){
			file.mkdirs();
		}
		return true;
	}

	 public void updateMetaInfo(ProjectInfo project) {
	    projectInfo = project;
	  }


}