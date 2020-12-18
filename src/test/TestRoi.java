package test;

import ij.gui.Roi;

public class TestRoi {

	Roi roi;
	String roiType;
	
	public TestRoi(Roi roi, String roiType) {
		super();
		this.roi = roi;
		this.roiType = roiType;
	}
	public Roi getRoi() {
		return roi;
	}
	public void setRoi(Roi roi) {
		this.roi = roi;
	}
	public String getRoiType() {
		return roiType;
	}
	public void setRoiType(String roiType) {
		this.roiType = roiType;
	}
	
}
