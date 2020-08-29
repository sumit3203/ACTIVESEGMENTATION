package activeSegmentation.prj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Sumit Vohra
 *
 */
public class FeatureInfo {

	private String key;
	private String label;
	private String zipFile;
	private int color;
	private Map<String, List<String>> trainingList= new HashMap<>();
	private Map<String, List<String>> testingList= new HashMap<>();
	
	
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getZipFile() {
		return zipFile;
	}
	
	public void setZipFile(String zipFile) {
		this.zipFile = zipFile;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public Map<String, List<String>> getTrainingList() {
		return trainingList;
	}
	
	public void setTrainingList(Map<String, List<String>> trainingList) {
		this.trainingList = trainingList;
	}
	
	public Map<String, List<String>> getTestingList() {
		return testingList;
	}
	
	public void setTestingList(Map<String, List<String>> testingList) {
		this.testingList = testingList;
	}

	public void addTrainingRois(String key, List<String> roisName) {
		this.trainingList.put(key, roisName);
	}
	
	public void addTestingRois(String key, List<String> roisName) {
		this.testingList.put(key, roisName);
	}
}
