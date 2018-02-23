package activeSegmentation.unitTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ijaux.scale.ZernikeMoment;
import ijaux.scale.Zps;

public class Test_Zernike_Radial_Poly {
	/*
	 * Taken a example of image size 50*50.
	 */
	public int image_width=50;
	public int image_height=50;
	public int centerX = image_width / 2;
    public int centerY = image_height / 2;
    final  int max = Math.max(centerX, centerY);
    public double radius = Math.sqrt(2 * max * max);
	
    int order = 4;
    int degree = 8;
    
    // Array of experimentally calculated radial polynomial values of given order and degree
    double [] radial_exp_values = null;  
    // // Array of manually calculated radial polynomial values of given order and degree
    double [] radial_manual_values = null;
    
	ZernikeMoment zm=new ZernikeMoment(8);

	
	public void extract_radial_exp(ImagePlus imp){
		radial_exp_values = new double[2500];
		Zps[] zps=new Zps[image_height*image_width];
		int index = 0;
		for(int i=0;i<image_height;i++){
        	for(int j=0;j<image_width;j++){
        		final int x = j-centerX;
        		final int y = i-centerY;
        		final double r = Math.sqrt((x * x) + (y * y)) / radius;
        		//zps[index]=new Zps(order,degree);
        		radial_exp_values[index] = zm.calculateRadial(r, order, degree, null);
        		index++;
        	}
        }
	}
	
	public void extract_radial_manual(ImagePlus imp){
		
		int ind = 0;
		radial_manual_values = new double[2500];
		try {
			BufferedReader br = new BufferedReader(new FileReader("../other_res/Zr"));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(!line.isEmpty()){
		    		radial_manual_values[ind]=Double.parseDouble(line.replace(";", ""));        		
		    		ind++;
		    	}	
		    }    
		    br.close();
		}catch (IOException e) {
		        e.printStackTrace();
			}
	}
	
	public void validate_radial_values(ImagePlus imp){
		
		if(radial_exp_values == null)
			extract_radial_exp(imp);
		
		if(radial_manual_values == null)
			extract_radial_manual(imp);

		int count = 0;
		for(int i=0;i<2500;i++){
			String exp=String.format("%.14f", radial_exp_values[i]);
    		String mann=String.format("%.14f", radial_manual_values[i]);	
        	if(exp.equals(mann))
        		count++;
		}	
		System.out.println("Total number of features:- "+2500);
		System.out.println("Number of features matches:- "+count);
	}

	public double calculate_zernike_values(ImagePlus imp, double[] radial_values){
		int index = 0;
		double real = 0;
		for(int i=0;i<image_height;i++){
        	for(int j=0;j<image_width;j++){
        		final int x = j-centerX;
        		final int y = i-centerY;
        		double pixel = imp.getPixel(x, y)[0];
        		final double ang = order* Math.atan2(y, x);
        		real += (pixel * radial_values[index] * Math.cos(ang));
        		index++;
         	}
        }
		return real;
	}
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String path="../other_res/test_image.tif";
    	ImagePlus imp=IJ.openImage(path);
    	ImageConverter ic=new ImageConverter(imp);
    	ic.convertToGray8();
       	System.out.println("Size of the image:- "+imp.getWidth()+"*"+imp.getHeight());
		
       	Test_Zernike_Radial_Poly test = new Test_Zernike_Radial_Poly ();
       	test.validate_radial_values(imp);
       	double real_exp = test.calculate_zernike_values(imp, test.radial_exp_values);
       	double real_mann = test.calculate_zernike_values(imp, test.radial_manual_values);
       	
       	System.out.println("Zernike Value computed experimently:- "+real_exp);
       	System.out.println("Zernike Value computed mannually:- "+real_mann);
	}

}
