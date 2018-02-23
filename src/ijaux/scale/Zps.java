package ijaux.scale;

import java.util.ArrayList;

/*
 * This classes stores all Radial polynomial upto order m n for individual pixel.
 */
public class Zps {

	ArrayList<Double> real=new ArrayList<Double>();
	ArrayList<Double> imag=new ArrayList<Double>();
	public void setComplex(ArrayList<Double> real,ArrayList<Double> imag){
		this.real=real;
		this.imag=imag;
	}
	public ArrayList<Double> getReal(){
		return real;
	}
	public ArrayList<Double> getImaginary(){
		return imag;
	}
}
