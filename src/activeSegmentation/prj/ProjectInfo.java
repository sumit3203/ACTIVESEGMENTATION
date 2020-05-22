package activeSegmentation.prj;

import activeSegmentation.IClassifier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import ijaux.datatype.Pair;

import java.util.*;


@JsonInclude(JsonInclude.Include.NON_NULL) 
public class ProjectInfo{
	
	private String projectName;
	private String projectType;
	private String projectDescription;
	private String comment = "Default Comment";
	private String createdDate;
	private String modifyDate;
	private String version="0.0.0.1";
	private int classes;
	private String projectPath;
	private List<String> pluginPath;

	private String trainingStack;
	private String testingStack;
	private List<Map<String, String>> filters = new ArrayList<Map<String, String>>();
	private List<FeatureInfo> featureList = new ArrayList<FeatureInfo>();
	private Map<String, String> learning = new HashMap<String, String>();
	private String groundtruth;
	private String featureSelection;
	
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
	private Integer featureLength;
	

	////////////////////////////////////////////
	//  Methods
	///////////////////////////////////////////
	
	/**
	 * 
	 * @return String
	 */
	public String getComment()	{
		return this.comment;
	}
	
	/**
	 * 
	 * @param comment
	 */
	public void setComment(String comment)	{
		this.comment = comment;
	}

	/**
	 * 
	 * @return String
	 */
	public String getCreatedDate()	{
		return this.createdDate;
	}

	/**
	 * 
	 * @param createdDate
	 */
	public void setCreatedDate(String createdDate)	{
		this.createdDate = createdDate;
	}

	/**
	 * 
	 * @return
	 */
	public String getModifyDate()	{
		return this.modifyDate;
	}

	/**
	 * 
	 * @param modifyDate
	 */
	public void setModifyDate(String modifyDate)	{
		this.modifyDate = modifyDate;
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

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * 
	 * @return
	 */
	public String getProjectName()	{
		return this.projectName;
	}

	/**
	 * 
	 * @param projectName
	 */
	public void setProjectName(String projectName)	{
		this.projectName = projectName;
	}

	/**
	 * 
	 * @return
	 */
	public String getProjectDescription()	{
		return this.projectDescription;
	}

	/**
	 * 
	 * @param projectDescription
	 */
	public void setProjectDescription(String projectDescription)	{
		this.projectDescription = projectDescription;
	}

	/**
	 * 
	 * @return
	 */
	public String getProjectType()	{
		return this.projectType;
	}

	/**
	 * 
	 * @param projectType
	 */
	public void setProjectType(String projectType)	{
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

