package ijaux.moments.zernike;

import java.util.ArrayList;

/*
 * This classes stores all Radial polynomial up to order m n for individual pixel.
 */
public class Zps {

	ArrayList<Double> real=new ArrayList<>();
	ArrayList<Double> imag=new ArrayList<>();
	
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
