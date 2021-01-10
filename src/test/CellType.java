package test;

import java.util.List;

import weka.core.Instance;

public class CellType {

	String tiffile;
	String celltype;
	Instance instances;
	public CellType(String tiffile, String celltype, Instance instances) {
		super();
		this.tiffile = tiffile;
		this.celltype = celltype;
		this.instances = instances;
	}
	public String getTiffile() {
		return tiffile;
	}
	public void setTiffile(String tiffile) {
		this.tiffile = tiffile;
	}
	public String getCelltype() {
		return celltype;
	}
	public void setCelltype(String celltype) {
		this.celltype = celltype;
	}
	public Instance getInstances() {
		return instances;
	}
	public void setInstances(Instance instances) {
		this.instances = instances;
	}
	
}
