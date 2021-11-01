package test;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import weka.core.Instance;

public class CellRoi {

	int x;
	int y;
	int w;
	int h;
	int z;
	int d;
	String celltype;
	
	List<Instance> roiInstances;
	
	
	@JsonIgnore
	public List<Instance> getRoiInstances() {
		return roiInstances;
	}
	@JsonIgnore
	public void setRoiInstances(List<Instance> roiInstances) {
		this.roiInstances = roiInstances;
	}
	public int getD() {
		return d;
	}
	public void setD(int d) {
		this.d = d;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getW() {
		return w;
	}
	public void setW(int w) {
		this.w = w;
	}
	public int getH() {
		return h;
	}
	public void setH(int h) {
		this.h = h;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int z) {
		this.z = z;
	}
	public String getCelltype() {
		return celltype;
	}
	public void setCelltype(String celltype) {
		this.celltype = celltype;
	}
	
}
