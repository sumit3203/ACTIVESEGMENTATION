package com.customplugin.activeseg.filter_core;

import java.awt.Rectangle;
import ij.process.ImageProcessor;

public class GLCMTextureDescriptors {

	private int d;
	private int phi;
	private double [][] glcm ;
	private double meanx=0.0;
	private double meany=0.0;
	private double stdevx=0.0;
	private double stdevy=0.0;

	// No of gray levels, in a 8 bit Gray scale image, we have 256 different shades
	private final int GRAY_LEVELS = 256;


	// d is the pixel distance, phi is direction angle

	public GLCMTextureDescriptors(){
		/*this.d = d;
		this.phi = phi;*/
		glcm = new double [GRAY_LEVELS][GRAY_LEVELS];
	}

	// re-initialise GLCM so that it can be used for calculation of GLCM at different d-s and phi-s, with same object
	private void reinitialize_glcm(){
		for(int i=0;i<GRAY_LEVELS;i++){
			for (int j=0;j<GRAY_LEVELS;j++){
				glcm[i][j] = 0.0;
			}
		}
	}

	// Utility functions which calculates basic statistical values of GLCM matrix, used in calculation of various features

	private void calculate_mean_variance(){
		double [] px = new double [GRAY_LEVELS];
		double [] py = new double [GRAY_LEVELS];
		meanx=0.0;
		meany=0.0;
		stdevx=0.0;
		stdevy=0.0;

		for (int i=0;  i<GRAY_LEVELS; i++) {
			for (int j=0; j<GRAY_LEVELS; j++) {
				px[i] += glcm [i][j];
			}
		}

		for (int j=0;  j<GRAY_LEVELS; j++) {
			for (int i=0; i<GRAY_LEVELS; i++) {
				py[j] += glcm [i][j];
			}
		}

		for (int i=0;  i<GRAY_LEVELS; i++) {
			meanx += (i*px[i]);
			meany += (i*py[i]);
		}

		for (int i=0;  i<GRAY_LEVELS; i++) {
			stdevx += ((Math.pow((i-meanx),2))*px[i]);
			stdevy += ((Math.pow((i-meany),2))*py[i]);
		}

	}

	//returns second order Angular moment

	public double getAngular2ndMoment(){
		double asm = 0.0;
		for (int i=0;  i<GRAY_LEVELS; i++)  {
			for (int j=0; j<GRAY_LEVELS; j++) {
				asm += (glcm[i][j]*glcm[i][j]);
			}
		}
		return asm;
	}

	// returns dissimilarity

	public double getDissimilarity(){
		double ds=0.0;

		for (int i=0;  i<GRAY_LEVELS; i++)  {
			for (int j=0; j<GRAY_LEVELS; j++) {
				ds += Math.abs(i-j)*(glcm[i][j]);
			}
		}
		return ds;
	}

	//returns contrast

	public double getContrast(){
		double contrast=0.0;

		for (int i=0;  i<GRAY_LEVELS; i++)  {
			for (int j=0; j<GRAY_LEVELS; j++) {
				contrast += Math.pow(i-j,2)*(glcm[i][j]);
			}
		}
		return contrast;
	}

	//returns energy

	public double getEnergy(){
		return Math.pow(getAngular2ndMoment(),0.5);
	}

	//returns entropy, measure of randomness

	public double getEntropy(){
		double entropy = 0.0;
		for (int i=0;  i<GRAY_LEVELS; i++)  {
			for (int j=0; j<GRAY_LEVELS; j++) {
				if (glcm[i][j] != 0) {
					entropy = entropy-(glcm[i][j]*(Math.log(glcm[i][j])));
				}
			}
		}
		return entropy;
	}

	//returns homogeneity

	public double getHomogeneity(){
		double homogeneity = 0.0;
		for (int i=0;  i<GRAY_LEVELS; i++) {
			for (int j=0; j<GRAY_LEVELS; j++) {
				homogeneity += glcm[i][j]/(1.0+Math.pow((i-j),2));
			}
		}
		return homogeneity;
	}

	//returns correlation

	public double getCorrelation(){
		double correlation=0.0;
		for (int i=0;  i<GRAY_LEVELS; i++) {
			for (int j=0; j<GRAY_LEVELS; j++) {
				if(stdevy==0 || stdevx ==0 ){
					return 1;
				}
				correlation += ((((i-meanx)*(j-meany))/Math.sqrt(stdevx*stdevy))*glcm[i][j]);
			}
		}
		return correlation;
	}


	//Here extraction of GLCM starts, returns normalised GLCM

	public double [][] extractGLCMDescriptors(ImageProcessor ip){
		reinitialize_glcm();
		// use the bounding rectangle ROI to roughly limit processing
		Rectangle roi = ip.getRoi();
		// get byte arrays for the image pixels and mask pixels
		int width = ip.getWidth();
		int height = ip.getHeight();
		byte [] pixels = (byte []) ip.getPixels();
		byte [] mask = ip.getMaskArray();
		int value;
		int dValue;
		double pixelCount = 0;

		int offsetX;
		int offsetY;

		double rad = Math.toRadians(-1.0 * phi);
		offsetX = (int) (d* Math.round(Math.cos(rad)));
		offsetY = (int) (d* Math.round(Math.sin(rad)));

		for (int y=roi.y; y<(roi.y + roi.height); y++) 	{
			for (int x=roi.x; x<(roi.x + roi.width); x++){
				if ((mask == null) || ((0xff & mask[(((y-roi.y)*roi.width)+(x-roi.x))]) > 0)) {
					int dx = x + offsetX;
					int dy = y + offsetY;
					if ( ((dx >= roi.x) && (dx < (roi.x+roi.width))) && ((dy >= roi.y) && (dy < (roi.y+roi.height))) ) {
						if ((mask == null) || ((0xff & mask[(((dy-roi.y)*roi.width)+(dx-roi.x))]) > 0) ) {
							value = 0xff & pixels[(y*width)+x];
							dValue = 0xff & pixels[(dy*width) + dx];
							glcm [value][dValue]++;		  			
							pixelCount++;
						}
					}
				}
			}
		}

		// convert the GLCM to Normalised-GLCM
		for (int i=0; i<GRAY_LEVELS; i++)  {
			for (int j=0; j<GRAY_LEVELS; j++) {
				glcm[i][j] = (glcm[i][j])/(pixelCount);
			}
		}

		calculate_mean_variance();

		return glcm;
	}

	// for reuse of GLCMTextureDescriptors object with different d-s and phi-s
	public void set_values(int d, int phi){
		this.d = d;
		this.phi = phi;
	}

}