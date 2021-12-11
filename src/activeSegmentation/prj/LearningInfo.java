package activeSegmentation.prj;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import weka.classifiers.AbstractClassifier;

/**
 * 
 * @author Dimiter Prodanov
 *  This is metadata class for handling of learning options.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class LearningInfo {
	
	@JsonIgnore
	AbstractClassifier classifier;
	
	private List<String> optionList = new ArrayList<>();
	
	private String featureSelection="";
	
	private String learningOption="";
	
	private String classifierfile="classifier.model";
	
	private String arff="trainingdata.arff";
	
	/**
	 * constructor
	 */
	public LearningInfo() {}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getOptionList(){
		return optionList;
	}
	
	/**
	 *  to be used for loading
	 * @return
	 */
	public String[] getOptionsArray() {
		Object[] objarr=optionList.toArray();
		String[] strar=new String[objarr.length];	
		for (int i=0;i<objarr.length;i++) {
			 Object obj=objarr[i]; 
			 strar[i]= (String) obj;
		}		
		return strar;
	}
	
	/**
	 * 
	 */
	public void updateOptionList() {
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

	/**
	 * 
	 * @return
	 */
	public String getClassifierFile() {
		return classifierfile;
	}

	/**
	 * 
	 * @param classifierfile
	 */
	public void setClassifierFile(String classifierfile) {
		this.classifierfile = classifierfile;
	}

	/**
	 * 
	 * @return
	 */
	public String getArffFile() {
		return arff;
	}

	/**
	 * 
	 * @param arff
	 */
	public void setArffFile(String arff) {
		this.arff = arff;
	}

	/**
	 * 
	 * @return
	 */
	public String getLearningOption() {
		return learningOption;
	}

	/**
	 * 
	 * @param learningOption
	 */
	public void setLearningOption(String learningOption) {
		this.learningOption = learningOption;
	}
	
	/**
	 * for debug purposes
	 */
	@Override
	public String toString( ) {
		String ret= "options="+optionList.toString()+"\n";
		ret+="featureSelection="+featureSelection+"\n";
		ret+="learningOption="+learningOption+"\n";
		return ret;
	}
	
}
