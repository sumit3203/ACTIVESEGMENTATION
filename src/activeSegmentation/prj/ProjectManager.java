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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import activeSegmentation.ASCommon;
import activeSegmentation.IDataSet;
import activeSegmentation.ProjectType;

public class ProjectManager {

	private IDataSet dataSet;
	private static ProjectInfo projectInfo;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private String activeSegDir;
	private Map<String,String> projectDir=new HashMap<>();

	public IDataSet getDataSet() {
		return  dataSet;
	}


	public void setData(IDataSet data) {
		dataSet = data.copy();
	}
	

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean loadProject(String fileName) {
		//System.out.println("IN LOAD PROJCT");
		IJ.log("loading project");
		setDirectory();
		//IJ.log(System.getProperty("plugins.dir"));
		if(projectInfo==null){
			ObjectMapper mapper = new ObjectMapper();
			try {
				//System.out.println(fileName);
				File projectFile=new File(fileName);
				//System.out.println(projectFile.getParent());
				projectInfo= mapper.readValue(projectFile, ProjectInfo.class);
				//projectInfo.setPluginPath(activeSegDir);
				//metaInfo.setPath(path);
				//System.out.println("done");
				//System.out.println( projectInfo.getProjectName());
				setProjectDir(projectFile.getParent(), null);
				projectInfo.setProjectDirectory(projectDir);
				//System.out.println(projectInfo.toString());
				
			} catch (UnrecognizedPropertyException e) {
				e.printStackTrace();
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			


		}

		return true;
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
			//System.out.println("SAVING");
			mapper.writeValue(new File(projectInfo.getProjectPath()+
					"/"+projectInfo.projectName+
					"/"+projectInfo.projectName+".json"), projectInfo);

			//System.out.println("DONE");

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
	 * @param projectDirectory
	 * @param projectDescription
	 * @param trainingImage
	 * @return
	 */
	public String createProject(String projectName, String projectType,String projectDirectory, 
			String projectDescription,
			String trainingImage){
		String message="done";
		String returnedMessage=validate(projectName,projectDirectory,trainingImage);
		if(!returnedMessage.equalsIgnoreCase(message)) {
			return returnedMessage;
		}
		setDirectory();
		projectInfo= new ProjectInfo();
		projectInfo.setProjectPath(projectDirectory);
	
		projectInfo.projectName=projectName;
		projectInfo.setProjectType(ProjectType.valueOf(projectType));

		projectInfo.projectDescription=projectDescription;
		List<String> jars= new ArrayList<>();
		jars.add(activeSegDir);
		projectInfo.setPluginPath(jars);
		//DEFAULT 2 classes
		projectInfo.setClasses(2);
		createProjectSpace(projectDirectory,projectName);
		//CURRENT IMAGE
		if(null !=WindowManager.getCurrentImage()) {
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
				List<String> images=loadImages(trainingImage);
				for(String image: images) {
					ImagePlus currentImage=IJ.openImage(trainingImage+"/"+image);
					createImages(image, currentImage);
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
		String format=image.substring(image.lastIndexOf("."));
		String folder=image.substring(0, image.lastIndexOf("."));	
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
			String title= folder+i;
			IJ.log(folder);
			IJ.log(title);
			createDirectory(projectDir.get(ASCommon.K_FILTERSDIR)+title);
			IJ.saveAs(new ImagePlus(title, processor),format,projectDir.get(ASCommon.K_IMAGESDIR)+title);
		}
		IJ.log("createStackdone");
	}
	
	/**
	 * 
	 * @param projectName
	 * @param projectDirectory
	 * @param trainingImage
	 * @return
	 */
	private String validate(String projectName,String projectDirectory, 
			String trainingImage) {
		String message="done";
		if(projectName==null|| projectName.isEmpty()) {
			return " Project Name cannot be Empty";

		} else if(projectDirectory==null|| projectDirectory.isEmpty() || projectDirectory.equalsIgnoreCase(trainingImage)) {
			return "Project Directory cannot be Empty and Should not be same as training image directory";
		}
		else if (null == WindowManager.getCurrentImage() &&(trainingImage==null|| trainingImage.isEmpty())) {
			return "Training cannot be Empty and should be either tif file or folder with tiff images are"
					+ "located";
		}
		return message;
	}

	/**
	 * 
	 */
	private void setDirectory() {
		//IJ.debugMode=true;
		String OS = System.getProperty("os.name").toLowerCase();
		IJ.log(OS);
		//check for null here
		String plugindir=System.getProperty("plugins.dir");
		if (plugindir==null) throw new RuntimeException("plugins.dir not set.");
		
		if( (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 )) {
			activeSegDir=plugindir+"//plugins//activeSegmentation//ACTIVE_SEG.jar";
		}
		else {
			activeSegDir=plugindir+"\\plugins\\activeSegmentation\\ACTIVE_SEG.jar";	
		}

		//System.out.println(System.getProperty("plugins.dir"));
	}

	/**
	 * 
	 * @param projectDirectory
	 * @param projectName
	 */
	private void setProjectDir(String projectDirectory, String projectName) {
		String projectString;
		if(projectName!=null) {
			projectString=projectDirectory+"/"+projectName+"/"+"training";
		}else {
			projectString=projectDirectory+"/"+"Training";
		}
		
		projectDir.put(ASCommon.K_PROJECTDIR, projectString);
		/*
		projectDir.put(ASCommon.K_FILTERSDIR, projectString+"/filters/");
		projectDir.put(ASCommon.K_FEATURESDIR, projectString+"/features/");
		projectDir.put(ASCommon.K_LEARNINGDIR, projectString+"/learning/");
		projectDir.put(ASCommon.K_EVALUATIONDIR,projectString+"/evaluation/");
		projectDir.put(ASCommon.K_IMAGESDIR,projectString+"/images/");
		projectDir.put(ASCommon.K_TESTIMAGESDIR,projectString+"/testimages/");
		projectDir.put(ASCommon.K_TESTFILTERDIR,projectString+"/testfilters/");
		*/
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
	
	private File[] sortImages(File[] images) {
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
	
	private List<String> loadImages(String directory){
		List<String> imageList= new ArrayList<String>();
		File folder = new File(directory);
		File[] images = sortImages(folder.listFiles());
		
		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}
		
		return imageList;
	}
	
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