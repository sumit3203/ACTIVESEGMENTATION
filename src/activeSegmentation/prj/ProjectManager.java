package activeSegmentation.prj;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

//import activeSegmentation.IProjectManager;
import activeSegmentation.ASCommon;
import activeSegmentation.IDataSet;
import activeSegmentation.ProjectType;
import activeSegmentation.learning.WekaDataSet;
import weka.core.Instances;

public class ProjectManager {

	private IDataSet dataSet;
	private static ProjectInfo projectInfo;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private String activeSegDir;
	private Map<String,String> projectDir=new HashMap<String,String>();
	
	/**
	 * Read ARFF file
	 * @param filename ARFF file name
	 * @return set of instances read from the file
	 */
	public IDataSet readDataFromARFF(String filename){
		try{
			BufferedReader reader = new BufferedReader(
					new FileReader(filename));
			try{
				Instances data = new Instances(reader);
				// setting class attribute
				data.setClassIndex(data.numAttributes() - 1);
				reader.close();
				return new WekaDataSet(data);
			} catch(IOException e){
				e.printStackTrace();
				IJ.showMessage("IOException");}
		}	catch(FileNotFoundException e){IJ.showMessage("File not found!");}
		return null;
	}

	//@Override
	public IDataSet getDataSet() {
		return  dataSet;
	}

	//@Override
	public void setData(IDataSet data) {
		dataSet = data.copy();
	}

	//TODO use the default from the interface
	//@Override
	public boolean writeDataToARFF(Instances data, String filename)	{
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream( projectInfo.getProjectPath()+filename ) ) );

			final Instances header = new Instances(data, 0);
			out.write(header.toString());

			for(int i = 0; i < data.numInstances(); i++)			{
				out.write(data.get(i).toString()+"\n");
			}
		}	catch(Exception e)		{
			IJ.log("Error: couldn't write instances into .ARFF file.");
			IJ.showMessage("Exception while saving data as ARFF file");
			e.printStackTrace();
			return false;
		}	finally{
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;

	}

	//@Override
	public boolean loadProject(String fileName) {
		//System.out.println("IN LOAD PROJCT");
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

	//@Override
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
			//mapper.writeValue(new File(projectInfo.getProjectPath()+"/"+projectInfo.getProjectName()+"/"+projectInfo.getProjectName()+".json"), projectInfo);
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

	//@Override
	public ProjectInfo getMetaInfo() {

		return projectInfo;
	}

	//@Override
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
		//projectInfo.setProjectName(projectName);
		projectInfo.projectName=projectName;
		projectInfo.setProjectType(ProjectType.valueOf(projectType));
		//projectInfo.setProjectDescription(projectDescription);
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


	private void createImages(String image, ImagePlus currentImage) {
		String format=image.substring(image.lastIndexOf("."));
		String folder=image.substring(0, image.lastIndexOf("."));	
		if(currentImage.getStackSize()>0) {
			createStackImage(currentImage,format,folder);
		}else {
			createDirectory(projectDir.get(ASCommon.FILTERSDIR)+folder);
			IJ.saveAs(currentImage,format,projectDir.get(ASCommon.IMAGESDIR)+folder);

		}

	}
	
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
			createDirectory(projectDir.get(ASCommon.FILTERSDIR)+title);
			IJ.saveAs(new ImagePlus(title, processor),format,projectDir.get(ASCommon.IMAGESDIR)+title);
		}
		IJ.log("createStackdone");
	}
	
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

	private void setDirectory() {
		//IJ.debugMode=true;
		String OS = System.getProperty("os.name").toLowerCase();
		IJ.log(OS);
		if( (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 )) {
			activeSegDir=System.getProperty("plugins.dir")+"//plugins//activeSegmentation//ACTIVE_SEG.jar";
		}
		else {
			activeSegDir=System.getProperty("plugins.dir")+"\\plugins\\activeSegmentation\\ACTIVE_SEG.jar";	
		}

		//System.out.println(System.getProperty("plugins.dir"));
	}

	private void setProjectDir(String projectDirectory, String projectName) {
		String projectString;
		if(projectName!=null) {
			projectString=projectDirectory+"/"+projectName+"/"+"Training";
		}else {
			projectString=projectDirectory+"/"+"Training";
		}
		
		projectDir.put(ASCommon.PROJECTDIR, projectString);
		projectDir.put(ASCommon.FILTERSDIR, projectString+"/filters/");
		projectDir.put(ASCommon.FEATURESDIR, projectString+"/features/");
		projectDir.put(ASCommon.LEARNINGDIR, projectString+"/learning/");
		projectDir.put(ASCommon.EVALUATIONDIR,projectString+"/evaluation/");
		projectDir.put(ASCommon.IMAGESDIR,projectString+"/images/");
	}
	
	private void createProjectSpace(String projectDirectory, String projectName) {

		setProjectDir(projectDirectory, projectName);
		createDirectory(projectDir.get(ASCommon.PROJECTDIR));
		createDirectory(projectDir.get(ASCommon.FILTERSDIR));
		createDirectory(projectDir.get(ASCommon.FEATURESDIR));
		createDirectory(projectDir.get(ASCommon.LEARNINGDIR));
		createDirectory(projectDir.get(ASCommon.EVALUATIONDIR));
		createDirectory(projectDir.get(ASCommon.IMAGESDIR));
		IJ.log("DONE");
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