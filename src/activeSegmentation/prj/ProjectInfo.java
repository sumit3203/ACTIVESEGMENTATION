package activeSegmentation.prj;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.IClassifier;
import activeSegmentation.ProjectType;
import ijaux.datatype.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Project metadata structure
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectInfo{
	
	///////////////////
	// Public fields
	///////////////////
	
	public String projectName;
	
	public String projectDescription="Default description";
	
	public String comment = "Default Comment";
	
	////////////////////
	// Private fields
	////////////////////
	
	private ProjectType projectType=null;
	private Date createdDate=new Date();
	private Date modifyDate=new Date();
	
	private String version="1.0.0";
	
	@JsonProperty(value="classes")
	private int classes=-1;
	 
	private String projectPath="";
	
	private List<String> pluginPath=null;

	private String trainingStack="";
	
	private String testingStack="";
	
	private List<Map<String, String>> filters = new ArrayList<Map<String, String>>();
	
	private List<FeatureInfo> featureList = new ArrayList<FeatureInfo>();
	
	private Map<String, String> learning = new HashMap<String, String>();
	
	private String groundtruth="";
	
	private String featureSelection="";
	
	/*
	 * JSON ignore
	 */
	@JsonIgnore
	private Map<String, String> projectDirectory = new HashMap<String,String>();
	
	@JsonIgnore
	private Map<String, Set<String>> featureNames = new HashMap<String, Set<String>>();
	
	@JsonIgnore
	private Map<String,List<Pair<String,double[]>>> features=new HashMap<String,List<Pair<String,double[]>>>();
	
	@JsonIgnore
	private IClassifier classifier;
	
	@JsonIgnore
	private int featureLength;
	

	////////////////////////////////////////////
	//  Methods
	///////////////////////////////////////////
  

	/**
	 * 
	 * @return String
	 */
	public String getCreatedDate()	{
		return sdf.format(createdDate);
	}

	/**
	 * 
	 * @param createdDate
	 */
	public void setCreatedDate(String createdDate)	{
	
		try {
			this.createdDate = sdf.parse(createdDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private	SimpleDateFormat sdf=new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");	

	/**
	 * 
	 * @return
	 */
	public String getModifyDate()	{
		return sdf.format(modifyDate);
	}

	/**
	 * 
	 * @param modifyDate
	 */
	public void setModifyDate(String modifyDate)	{
		try {
			this.modifyDate = sdf.parse(modifyDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return List<Map<String, String>>
	 */
	public List<Map<String, String>> getFilters()	{
		return this.filters;
	}

	/**
	 * 
	 * @param filters
	 */
	public void setFilters(List<Map<String, String>> filters)	{
		this.filters = filters;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<FeatureInfo> getFeatureList()	{
		return this.featureList;
	}

	/**
	 * 
	 * @param featureList
	 */
	public void setFeatureList(List<FeatureInfo> featureList)	{
		this.featureList = featureList;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getLearning()	{
		return this.learning;
	}

	/**
	 * 
	 * @param learning
	 */
	public void setLearning(Map<String, String> learning)	{
		this.learning = learning;
	}

	/**
	 * 
	 * @param featureInfo
	 */
	public void addFeature(FeatureInfo featureInfo)	{
		this.featureList.add(featureInfo);
	}

	/**
	 * 
	 */
	public void resetFeatureInfo()	{
		this.featureList.clear();
	}

	/**
	 * 
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @TODO version format decision
	 * 
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}



	public ProjectType getProjectType() {
		return projectType;
	}

	public void setProjectType(ProjectType projectType) {
		this.projectType = projectType;
	}

	/**
	 * 
	 * @return
	 */
	public String getTrainingStack()	{
		return this.trainingStack;
	}

	/**
	 * 
	 * @return
	 */
	public String getProjectPath()	{
		return this.projectPath;
	}
	
	/**
	 * 
	 * @param projectPath
	 */
	public void setProjectPath(String projectPath) 	{
		this.projectPath = projectPath;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getProjectDirectory() 	{
		return this.projectDirectory;
	}

	/**
	 * 
	 * @param projectDirectory
	 */
	public void setProjectDirectory(Map<String, String> projectDirectory){
		this.projectDirectory = projectDirectory;
	}

	/**
	 * 
	 * @param trainingStack
	 */
	public void setTrainingStack(String trainingStack)	{
		this.trainingStack = trainingStack;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTestingStack()	{
		return this.testingStack;
	}

	/**
	 * 
	 * @return
	 */
	public String getFeatureSelection()	{
		return this.featureSelection;
	}
	
	/**
	 * 
	 * @param featureSelection
	 */
	public void setFeatureSelection(String featureSelection)	{
		this.featureSelection = featureSelection;
	}

	/**
	 * 
	 * @param testingStack
	 */
	public void setTestingStack(String testingStack)	{
		this.testingStack = testingStack;
	}

	/**
	 * 
	 * @return
	 */
	public int getClasses()	{
		return this.classes;
	}

	/**
	 * 
	 * @param classes
	 */
	public void setClasses(int classes)	{
		this.classes = classes;
	}

	/**
	 * 
	 * @return
	 */
	public IClassifier getClassifier()	{
		return this.classifier;
	}

	/**
	 * 
	 * @param classifier
	 */
	public void setClassifier(IClassifier classifier)	{
		this.classifier = classifier;
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getFeatureLength() {
		return featureLength;
	}

	/**
	 * 
	 * @param featureLength
	 */
	public void setFeatureLength(Integer featureLength) {
		this.featureLength = featureLength;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, List<Pair<String, double[]>>> getFeatures() {
		return features;
	}

	/**
	 * 
	 * @param features
	 */
	public void setFeatures(Map<String, List<Pair<String, double[]>>> features) {
		this.features = features;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, Set<String>> getFeatureNames() {
		return featureNames;
	}

	/**
	 * 
	 * @param featureNames
	 */
	public void setFeatureNames(Map<String, Set<String>> featureNames) {
		this.featureNames = featureNames;
	}

	/**
	 * 
	 * @return
	 */
	public String getGroundtruth() {
		return groundtruth;
	}
	
	/**
	 * 
	 * @param groundtruth
	 */
	public void setGroundtruth(String groundtruth) {
		this.groundtruth = groundtruth;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getPluginPath() {
		//i think this will be null
		return pluginPath;
	}

	/**
	 * 
	 * @param pluginPath
	 */
	public void setPluginPath(List<String> pluginPath) {
		this.pluginPath = pluginPath;
	}
	
}// END

