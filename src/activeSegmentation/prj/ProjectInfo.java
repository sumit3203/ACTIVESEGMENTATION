package activeSegmentation.prj;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.ProjectType;
import ijaux.datatype.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Project metadata structure
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class ProjectInfo{
	
	///////////////////
	// Public fields
	///////////////////
	
	public String projectName="Project 1";
	
	public String projectDescription="Default description";
	
	public String comment = "Default Comment";
	
	public String version="1.0.7";

	public final static String compatibleVersion="1.0.7";
	
	public String helpURL=""; 
	
	////////////////////
	// Private fields
	////////////////////
	
	private ProjectType projectType=null;
	private Date createdDate=new Date();
	private Date modifyDate=new Date();
		
	
	@JsonProperty(value="classes")
	private int classes=-1;
	 
	private String projectPath="";
	
	private List<String> pluginPath=null;

	private String trainingStack="";
	
	private String testingStack="";
	
	private List<Map<String, String>> filters = new ArrayList<>();
	
	private List<FeatureInfo> featureList = new ArrayList<>();
	
	private String groundtruth="";
	
	@JsonProperty(value="learning") 
	private LearningInfo learning = new LearningInfo();
	
	/*
	 * JSON ignore part
	 */
	@JsonIgnore
	private Map<String, String> projectDirectory = new HashMap<>();
	
	@JsonIgnore
	private Map<String, Set<String>> featureNames = new HashMap<>();
	
	@JsonIgnore
	private Map<String,List<Pair<String, double[]>>> features=new HashMap<>();
		
	@JsonIgnore
	private int featureLength;
	
	@JsonIgnore
	private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");	

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
		return filters;
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
		return featureList;
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
	public LearningInfo getLearning()	{
		return learning;
	}

	/**
	 * 
	 * @param learning
	 */
	public void setLearning(LearningInfo learning)	{
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
	public boolean lesserVersion(String version) {
		final int result= this.version.compareToIgnoreCase(version);
		return (result<0);
	}
	
	public boolean greaterVersion(String version) {
		final int result= this.version.compareToIgnoreCase(version);
		return (result>=0);
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * 
	 * @return SEGM: SEGM / CLASSIF
	 */
	public ProjectType getProjectType() {
		return projectType;
	}
	
	/**
	 * 
	 * @param projectType
	 */
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
		return testingStack;
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
		return classes;
	}

	/**
	 * 
	 * @param classes
	 */
	public void setNClasses(int classes)	{
		this.classes = classes;
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
	 * @return pluginPath
	 */
	public List<String> getPluginPath() {
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

