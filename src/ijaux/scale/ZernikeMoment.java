package ijaux.scale;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class ZernikeMoment {
	int degree;
	int centerX;
	int centerY;
	double radius;
	public RadialValue[] rv=null;
	
	public ZernikeMoment(int degree){
		this.degree=degree;
	}
	
	public ZernikeMoment(int degree, RadialValue[] rv){
		this.degree = degree;
		this.rv = rv;
	}
	
	public double calculateRadial(double r, int m, int n, RadialValue rv){
		
		if(n<m || n<0 || m<0)
			return 0;
		
		//Check if radial value for m and n already present.
		if(rv!=null && rv.get(m, n)!=0)
			return rv.get(m, n);
		
		if(n==0&&m==0)
			return 1;
		
		if((n-m)%2==0)
			return (r*(calculateRadial(r,Math.abs(m-1),n-1,rv)+calculateRadial(r,m+1,n-1,rv))-calculateRadial(r,m,n-2,rv));	
		
		else
			return 0;
	}
	
	public void calculateRadius(ImageProcessor ip){
		centerX = ip.getWidth() / 2;
        centerY = ip.getHeight() / 2;
        final int max = Math.max(centerX, centerY);
        radius = Math.sqrt(2 * max * max);
	}

	public Complex extractZernikeMoment(ImageProcessor ip){
		System.out.println("Start Zernike moment extraction process");
		calculateRadius(ip);
		
		ArrayList<Double> real = null; 
    	ArrayList<Double> imag = null;
    	if(rv==null)
    		rv = new RadialValue[ip.getHeight()*ip.getWidth()];
    
    	Zps[] zps=new Zps[ip.getHeight()*ip.getWidth()];
    	int index=0;
        for(int i=0;i<ip.getHeight();i++){
        	for(int j=0;j<ip.getWidth();j++){
        		final int x = j-centerX;
        		final int y = i-centerY;
        		final double r = Math.sqrt((x * x) + (y * y)) / radius;
        		//For each pixel create zps object
        		
        		zps[index]=new Zps();
        		
        		if(rv[index]==null)
        			rv[index] = new RadialValue(degree,degree);
        		
        		real=new ArrayList<Double>();
        		imag=new ArrayList<Double>();
        		
        		for(int k=0;k<=degree;k++){
        			for(int l=0;l<=k;l++){
        				
        				if((k-l)%2==0){
        					//Calculate radial_value
        					double radial_value = calculateRadial(r, l, k, rv[index]);
        					final double ang = l * Math.atan2(y, x);
        					double pixel = ip.getPixel(x, y);
        	        		real.add(pixel * radial_value * Math.cos(ang)* (degree + 1));
        	        		imag.add(pixel * radial_value * Math.sin(ang)* (degree + 1));
        	        		rv[index].set(l, k, radial_value); 
        				}
        			}
        		}
        		
        		zps[index].setComplex(real, imag);
        		index++;
        		
        		 	
         	}
       }
        
        double[] real_result=new double[real.size()];
        double[] imag_result=new double[real.size()];
        for(int i=0;i<zps.length;i++){
        	ArrayList<Double> temp=zps[i].getReal();
        	for(int j=0;j<temp.size();j++){
        		real_result[j]+=(temp.get(j)) / Math.PI;
        	}
        	temp=zps[i].getImaginary();
        	for(int j=0;j<temp.size();j++){
        		imag_result[j]+=(temp.get(j)) / Math.PI;
        	}
        }
        /*for(int i=0;i<real_result.length;i++){
        	System.out.println("Real Value:-" +real_result[i]+" Imaginary Value:- "+ imag_result[i]);
        }*/
        
		return new Complex(real_result, imag_result);
	}
	public static void main(String[] args){
		String path="/home/mg/Downloads/tifs/image.tif";
    	ImagePlus imp=IJ.openImage(path);
    	ImageConverter ic=new ImageConverter(imp);
    	ic.convertToGray8();
    	
    	ImageProcessor ip=imp.getProcessor();
    	ZernikeMoment zm=new ZernikeMoment(8);
    	long aa=System.currentTimeMillis();
    	zm.extractZernikeMoment(ip);
    	long bb=System.currentTimeMillis();
    	System.out.println(bb-aa);
	}
	public static class Complex {
        /** real part. */
        private double[] m_real;

        /** imaginary part. */
        private double[] m_imaginary;

        /**
         * constructor for number with imaginary part = 0.
         * 
         * @param real the real part
         */
        public Complex(final double[] real) {
            m_real = real;
            m_imaginary = null;
        }

        /**
         * constructor.
         * 
         * @param real the real part
         * @param imaginary the imaginary part
         */
        public Complex(final double[] real, final double[] imaginary) {
            m_real = real;
            m_imaginary = imaginary;
        }

        /**
         * @return the real part of the complex number.
         */
        public double[] getReal() {
            return m_real;
        }

        /**
         * @return the imaginary part of the complex number.
         */
        public double[] getImaginary() {
            return m_imaginary;
        }

     }
}
