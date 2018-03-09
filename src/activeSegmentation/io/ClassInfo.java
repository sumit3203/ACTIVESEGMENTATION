package activeSegmentation.io;

import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassInfo {

	String key;
	String label;
	Color color;
    Map<String,List<Roi>> trainingRois;	
    Map<String,List<Roi>> testingRois;
    
    
	public ClassInfo(String key, String label, Color color,
			Map<String, List<Roi>> trainingRois,
			Map<String, List<Roi>> testingRois) {
		this.key = key;
		this.label = label;
		this.color = color;
		this.trainingRois = trainingRois;
		this.testingRois = testingRois;
	}
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
	
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public Map<String, List<Roi>> getTrainingRois() {
		return trainingRois;
	}
	public void setTrainingRois(Map<String, List<Roi>> trainingRois) {
		this.trainingRois = trainingRois;
	}
	public Map<String, List<Roi>> getTestingRois() {
		return testingRois;
	}
	public void setTestingRois(Map<String, List<Roi>> testingRois) {
		this.testingRois = testingRois;
	}	
    
	public void addTrainingRois(String key, Roi roi){
		if(this.trainingRois.containsKey(key)){
			this.trainingRois.get(key).add(roi);
		}
		else{
			List<Roi> rois= new ArrayList<Roi>();
			rois.add(roi);
			this.trainingRois.put(key, rois);
		}
	}
	
	public void addTestingRois(String key, Roi roi){
		if(this.testingRois.containsKey(key)){
			this.testingRois.get(key).add(roi);
		}
		else{
			List<Roi> rois= new ArrayList<Roi>();
			rois.add(roi);
			this.testingRois.put(key, rois);
		}
	}
	
	public int getTrainingRoiSize(String key){
		if(this.trainingRois.containsKey(key)){
			return this.trainingRois.get(key).size();
		}
		return 0;
	}
	
	public int getTestingRoiSize(String key){
		if(this.testingRois.containsKey(key)){
		return	this.testingRois.get(key).size();
		}
		return 0;
	}
}