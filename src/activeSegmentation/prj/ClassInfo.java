package activeSegmentation.prj;

import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Learning class information - ROIs etc
 * 
 * @author Sumit Vohra
 *
 */
public class ClassInfo {

	String key;
	String label;
	Color color;
	
	Map<String,List<Roi>> trainingRois;	
	Map<String,List<Roi>> testingRois;

	/**
	 * 
	 * @param key
	 * @param label
	 * @param color
	 * @param trainingRois
	 * @param testingRois
	 */
	public ClassInfo(String key, String label, Color color,
			Map<String, List<Roi>> trainingRois,
			Map<String, List<Roi>> testingRois) {
		this.key = key;
		this.label = label;
		this.color = color;
		this.trainingRois = trainingRois;
		this.testingRois = testingRois;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * 
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * 
	 * @param trainingRois
	 */
	public void setTrainingRois(Map<String, List<Roi>> trainingRois) {
		this.trainingRois = trainingRois;
	}

	/**
	 * 
	 * @param testingRois
	 */
	public void setTestingRois(Map<String, List<Roi>> testingRois) {
		this.testingRois = testingRois;
	}	
	
	/**
	 * 
	 * @return
	 */
	public  Set<String> getTrainingRoiSlices(){
		return this.trainingRois.keySet();
	}
	
	/**
	 * 
	 * @return
	 */
	public  Set<String> getTestingRoiSlices(){
		return this.testingRois.keySet();
	}
	
	/**
	 * 
	 * @param key
	 * @param roi
	 */
	public void addTrainingRois(String key, Roi roi){
		if(this.trainingRois.containsKey(key)){
			this.trainingRois.get(key).add(roi);
		} else{
			List<Roi> rois= new ArrayList<>();
			rois.add(roi);
			this.trainingRois.put(key, rois);
		}
	}

	/**
	 * 
	 * @param key
	 * @param roi
	 */
	public void addTestingRois(String key, Roi roi){
		if(this.testingRois.containsKey(key)){
			this.testingRois.get(key).add(roi);
		} else{
			List<Roi> rois= new ArrayList<>();
			rois.add(roi);
			this.testingRois.put(key, rois);
		}
	}
	
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	@JsonIgnore
	public int getTrainingRoiSize(String key){
		if(this.trainingRois.containsKey(key)){
			return this.trainingRois.get(key).size();
		}
		return 0;
	}
	 
	 /**
	  * 
	  * @param imageKey
	  * @param index
	  */
	@JsonIgnore
	public void deleteTrainingRoi(String imageKey, int index) {
		if(this.trainingRois.containsKey(imageKey)){
			this.trainingRois.get(imageKey).remove(index);
		}
	}
	
	/**
	 * 
	 * @param imageKey
	 * @param index
	 */
	@JsonIgnore
	public void deleteTestingRoi(String imageKey, int index) {
		if(this.testingRois.containsKey(imageKey)){
			this.testingRois.get(imageKey).remove(index);
		}
	}
	
	/**
	 * 
	 */
	@JsonIgnore
	public void deleteAllTestingRois() {
		testingRois.clear();
	}
	
	/**
	 * 
	 */
	@JsonIgnore
	public void deleteAllTrainingRois() {
		trainingRois.clear();
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public int getTestingRoiSize(String key){
		if(this.testingRois.containsKey(key)){
			return	this.testingRois.get(key).size();
		}
		return 0;
	}
	
	/**
	 * 
	 * @param imageKey
	 * @return
	 */
	public List<Roi> getTrainingRois(String imageKey){
		if(this.trainingRois.containsKey(imageKey)){
			return this.trainingRois.get(imageKey);
		}
		return null;
	}
	
	/**
	 * 
	 * @param imageKey
	 * @return
	 */
	public List<Roi> getTestingRois(String imageKey){
		if(this.testingRois.containsKey(imageKey)){
			return this.testingRois.get(imageKey);
		}	
		return null;
	}

	/**
	 * 
	 * @param imageKey
	 * @param index
	 * @return
	 */
	public Roi getTestingRoi(String imageKey, int index){
		if(this.testingRois.containsKey(imageKey)){
			return this.testingRois.get(imageKey).get(index);
		}	
		return null;
	}
	
	/**
	 * 
	 * @param imageKey
	 * @param index
	 * @return
	 */
	public Roi getTrainingRoi(String imageKey, int index){
		if(trainingRois.containsKey(imageKey)){
			return trainingRois.get(imageKey).get(index);
		}	
		return null;
	}
}