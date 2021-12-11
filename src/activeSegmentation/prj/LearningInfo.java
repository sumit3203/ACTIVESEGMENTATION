package activeSegmentation.prj;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import weka.classifiers.AbstractClassifier;

/**
 * 
 * @author prodanov
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class LearningInfo {
	
	public LearningInfo() {}
	
	@JsonIgnore
	AbstractClassifier classifier;
	
	private List<String> optionList = new ArrayList<>();
	
	private String featureSelection="";
	
	private String learningOption="";
	
	private String classifierfile="classifier.model";
	
	private String arff="trainingdata.arff";
	
	public List<String> getOptionList(){
		return optionList;
	}
	
	public void setOptionList() {
		String[] options=classifier.getOptions();
		optionList=new ArrayList<>();
		for (String s:options) 
			optionList.add(s);
	}
 	
	/**
	 * 
	 * @return
	 */
	public String getFeatureSelection()	{
		return featureSelection;
	}
	
	/**
	 * 
	 * @param featureSelection
	 */
	public void setFeatureSelection(String featureSelection)	{
		this.featureSelection=featureSelection;
	}

	
	/**
	 * 
	 * @return
	 */
	public AbstractClassifier getClassifier()	{
		return this.classifier;
	}

	/**
	 * 
	 * @param cls
	 */
	public void setClassifier(AbstractClassifier cls)	{
		this.classifier =  cls;
	}

	public String getClassifierFile() {
		return classifierfile;
	}

	public void setClassifierFile(String classifierfile) {
		this.classifierfile = classifierfile;
	}

	public String getArffFile() {
		return arff;
	}

	public void setArffFile(String arff) {
		this.arff = arff;
	}

	public String getLearningOption() {
		return learningOption;
	}

	public void setLearningOption(String learningOption) {
		this.learningOption = learningOption;
	}
	
	@Override
	public String toString( ) {
		String ret= "options="+optionList.toString()+"\n";
		ret+="featureSelection="+featureSelection+"\n";
		ret+="learningOption="+learningOption+"\n";
		return ret;
	}
	
}
