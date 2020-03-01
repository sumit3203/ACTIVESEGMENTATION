package activeSegmentation.prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.IClassifier;
import ijaux.scale.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectInfo{
	
	private String projectName;
	private String projectType;
	private String projectDescription;
	private String comment = "Default Comment";
	private String createdDate;
	private String modifyDate;
	private int classes;
	private String projectPath;
	private String pluginPath;
	@JsonIgnore
	private Map<String, String> projectDirectory = new HashMap<String,String>();
	private String trainingStack;
	private String testingStack;
	private List<Map<String, String>> filters = new ArrayList<Map<String, String>>();
	private List<FeatureInfo> featureList = new ArrayList<FeatureInfo>();
	private Map<String, String> learning = new HashMap<String, String>();
	private String groundtruth;

	

	@JsonIgnore
	private Map<String, Set<String>> featureNames = new HashMap<String, Set<String>>();
	@JsonIgnore
	private Map<String,List<Pair<String,double[]>>> features=new HashMap<String,List<Pair<String,double[]>>>();
	@JsonIgnore
	private IClassifier classifier;
	@JsonIgnore
	private Integer featureLength;
	private String featureSelection;

	public String getComment()	{
		return this.comment;
	}

	public void setComment(String comment)	{
		this.comment = comment;
	}

	public String getCreatedDate()	{
		return this.createdDate;
	}

	public void setCreatedDate(String createdDate)	{
		this.createdDate = createdDate;
	}

	public String getModifyDate()	{
		return this.modifyDate;
	}

	public void setModifyDate(String modifyDate)	{
		this.modifyDate = modifyDate;
	}

	public List<Map<String, String>> getFilters()	{
		return this.filters;
	}

	public void setFilters(List<Map<String, String>> filters)	{
		this.filters = filters;
	}

	public List<FeatureInfo> getFeatureList()	{
		return this.featureList;
	}

	public void setFeatureList(List<FeatureInfo> featureList)	{
		this.featureList = featureList;
	}

	public Map<String, String> getLearning()	{
		return this.learning;
	}

	public void setLearning(Map<String, String> learning)	{
		this.learning = learning;
	}

	public void addFeature(FeatureInfo featureInfo)	{
		this.featureList.add(featureInfo);
	}

	public void resetFeatureInfo()	{
		this.featureList.clear();
	}

	public String getProjectName()	{
		return this.projectName;
	}

	public void setProjectName(String projectName)	{
		this.projectName = projectName;
	}

	public String getProjectDescription()	{
		return this.projectDescription;
	}

	public void setProjectDescription(String projectDescription)	{
		this.projectDescription = projectDescription;
	}

	public String getProjectType()	{
		return this.projectType;
	}

	public void setProjectType(String projectType)	{
		this.projectType = projectType;
	}

	public String getTrainingStack()	{
		return this.trainingStack;
	}

	public String getProjectPath()	{
		return this.projectPath;
	}

	public void setProjectPath(String projectPath) 	{
		this.projectPath = projectPath;
	}

	public String getPluginPath() {
		return this.pluginPath;
	}

	public Map<String, String> getProjectDirectory() 	{
		return this.projectDirectory;
	}

	public void setProjectDirectory(Map<String, String> projectDirectory){
		this.projectDirectory = projectDirectory;
	}

	public void setPluginPath(String pluginPath){
		this.pluginPath = pluginPath;
	}

	public void setTrainingStack(String trainingStack)	{
		this.trainingStack = trainingStack;
	}

	public String getTestingStack()	{
		return this.testingStack;
	}

	public String getFeatureSelection()	{
		return this.featureSelection;
	}

	public void setFeatureSelection(String featureSelection)	{
		this.featureSelection = featureSelection;
	}

	public void setTestingStack(String testingStack)	{
		this.testingStack = testingStack;
	}

	public int getClasses()	{
		return this.classes;
	}

	public void setClasses(int classes)	{
		this.classes = classes;
	}

	public IClassifier getClassifier()	{
		return this.classifier;
	}

	public void setClassifier(IClassifier classifier)	{
		this.classifier = classifier;
	}
	
	public Integer getFeatureLength() {
		return featureLength;
	}

	public void setFeatureLength(Integer featureLength) {
		this.featureLength = featureLength;
	}


	public Map<String, List<Pair<String, double[]>>> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, List<Pair<String, double[]>>> features) {
		this.features = features;
	}

	public Map<String, Set<String>> getFeatureNames() {
		return featureNames;
	}

	public void setFeatureNames(Map<String, Set<String>> featureNames) {
		this.featureNames = featureNames;
	}

	public String getGroundtruth() {
		return groundtruth;
	}

	public void setGroundtruth(String groundtruth) {
		this.groundtruth = groundtruth;
	}
	
}

