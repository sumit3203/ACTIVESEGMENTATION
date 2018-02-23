package activeSegmentation.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureInfo {

	private int classLabel;
	private String zipFile;
	private Map<String, List<String>> sliceList= new HashMap<String, List<String>>();
	public int getClassLabel() {
		return classLabel;
	}
	public void setClassLabel(int classLabel) {
		this.classLabel = classLabel;
	}
	public String getZipFile() {
		return zipFile;
	}
	public void setZipFile(String zipFile) {
		this.zipFile = zipFile;
	}
	public Map<String, List<String>> getSliceList() {
		return sliceList;
	}
	public void setSliceList(Map<String, List<String>> sliceList) {
		this.sliceList = sliceList;
	}
	
	public void addSlice(String sliceName, List<String> rois){
		sliceList.put(sliceName, rois);
	}
	@Override
	public String toString() {
		return "FeatureInfo [classLabel=" + classLabel + ", zipFile=" + zipFile
				+ ", sliceList=" + sliceList + "]";
	}
	
}
