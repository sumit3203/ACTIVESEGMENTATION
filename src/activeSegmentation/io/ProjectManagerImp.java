package activeSegmentation.io;

import ij.IJ;
import ij.ImagePlus;

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
import java.util.Date;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import activeSegmentation.IProjectManager;
import activeSegmentation.IDataSet;

import activeSegmentation.learning.WekaDataSet;
import weka.core.Instances;

public class ProjectManagerImp implements IProjectManager {

	private IDataSet dataSet;
	private ProjectInfo projectInfo;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	

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
			}
			catch(IOException e){IJ.showMessage("IOException");}
		}
		catch(FileNotFoundException e){IJ.showMessage("File not found!");}
		return null;
	}

	@Override
	public IDataSet getDataSet() {

		return  dataSet;
	}

	@Override
	public void setData(IDataSet data) {
		this.dataSet = data.copy();
	}

	@Override
	/**
	 * Write current instances into an ARFF file
	 * @param data set of instances
	 * @param filename ARFF file name
	 */
	public boolean writeDataToARFF(Instances data, String filename)
	{
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream( projectInfo.getProjectPath()+filename ) ) );

			final Instances header = new Instances(data, 0);
			out.write(header.toString());

			for(int i = 0; i < data.numInstances(); i++)
			{
				out.write(data.get(i).toString()+"\n");
			}
		}
		catch(Exception e)
		{
			IJ.log("Error: couldn't write instances into .ARFF file.");
			IJ.showMessage("Exception while saving data as ARFF file");
			e.printStackTrace();
			return false;
		}
		finally{
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;

	}

	



	@Override
	public boolean loadProject(String fileName) {
		// TODO Auto-generated method stub
		System.out.println("IN LOAD PROJCT");
		if(projectInfo==null){
			ObjectMapper mapper = new ObjectMapper();
			try {
				projectInfo= mapper.readValue(new File(fileName), ProjectInfo.class);
				//metaInfo.setPath(path);
				System.out.println("done");

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

	@Override
	public void writeMetaInfo( ProjectInfo projectInfo) {
		this.projectInfo= projectInfo;
		ObjectMapper mapper = new ObjectMapper();
		try {
			projectInfo.setModifyDate(dateFormat.format(new Date()));
			if(projectInfo.getCreatedDate()==null){
				projectInfo.setCreatedDate(dateFormat.format(new Date()));
			}
			System.out.println("SAVING");
			mapper.writeValue(new File(projectInfo.getProjectPath()+"/"+projectInfo.getProjectName()+"/"+projectInfo.getProjectName()+".json"), projectInfo);

			System.out.println("DONE");

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ProjectInfo getMetaInfo() {
		
		return projectInfo;
	}

	@Override
	public ProjectInfo createProject(String projectName, Enum projectType,String projectDirectory, 
		     String projectDescription,
			String trainingImage,String pluginDir){

		projectInfo= new ProjectInfo();
		
		projectInfo.setProjectPath(projectDirectory);
		projectInfo.setProjectName(projectName);
		projectInfo.setProjectType(projectType);
		projectInfo.setProjectDescription(projectDescription);
		projectInfo.setPluginPath(pluginDir);
		//DEFAULT 2 classes
		projectInfo.setClasses(2);
		if(trainingImage!= null && !trainingImage.isEmpty()){
			ImagePlus trainingImagePlus=IJ.openImage(trainingImage);
			String projectString=projectDirectory+"/"+projectName+"/"+ "Training";
			System.out.println(projectString);
			createProjectSpace(projectString, trainingImagePlus.getImageStackSize());
			IJ.saveAs(trainingImagePlus,trainingImage.substring(trainingImage.lastIndexOf(".")),projectString+"/images/training");
			projectInfo.setTrainingStack(projectString+"/images/training"+trainingImage.substring(trainingImage.lastIndexOf(".")));
		}
		/*if(testImage!= null && !testImage.isEmpty()){
			ImagePlus testImagePlus=IJ.openImage(trainingImage);
			String projectString=path+projectName+"/"+ "Testing";
			createProjectSpace(projectString, testImagePlus.getImageStackSize());
			projectInfo.setTrainingStack(projectString+"/images/Testing."+trainingImage.substring(trainingImage.lastIndexOf(".")));
			IJ.saveAs(testImagePlus,testImage.substring(testImage.lastIndexOf(".")),projectString+"/filters");
		}*/
		writeMetaInfo(projectInfo);
		return projectInfo;
	}


	private void createProjectSpace(String projectString, int slices){
		createDirectory(projectString);
		createDirectory(projectString+"/images");
		createDirectory(projectString+"/filters");
		createDirectory(projectString+"/features");
		createDirectory(projectString+"/learning");
		createDirectory(projectString+"/evaluation");
		for(int i=1; i<=slices; i++){
			createDirectory(projectString+"/filters/SLICE-"+i);
		}
	}
	private boolean createDirectory(String project){

		File file=new File(project);
		if(!file.exists()){
			file.mkdirs();
		}
		return true;
	}

	@Override
	public ProjectInfo createProject(String projectName, String projectType, String projectDirectory,
			String projectDescription, String trainingImage, String testImage) {
		// TODO Auto-generated method stub
		return null;
	}




	

}
