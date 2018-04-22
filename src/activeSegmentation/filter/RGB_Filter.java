package activeSegmentation.filter;

import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class RGB_Filter {
	
	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "RGB";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "RGB";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RGB_Filter filter= new RGB_Filter();
		String filterPath="C:\\Users\\sumit\\Documents\\SEZEG\\preprocessed1\\";
		ImagePlus currentImage=IJ.openImage("C:\\Users\\sumit\\Documents\\SEZEG\\preprocessed1\\testpanel-01.tif");
		filter.applyFilter(currentImage.getProcessor(), filterPath);
		
	}
	
	public void applyFilter(ImageProcessor image, String filterPath) {
	
		int [] redImage= new int[image.getWidth()*image.getHeight()];
		int [] blueImage= new int[image.getWidth()*image.getHeight()];
		int [] greenImage= new int[image.getWidth()*image.getHeight()];
		float [] hImage= new float[image.getWidth()*image.getHeight()];
		float [] sImage= new float[image.getWidth()*image.getHeight()];
		float [] vImage= new float[image.getWidth()*image.getHeight()];
		for(int y=0; y<image.getHeight();y++) {
			
			for(int x=0; x<image.getWidth();x++) {
				
			int pixel=image.get(x, y);
			    int red = (pixel >> 16) & 0xff;
			    int green = (pixel >> 8) & 0xff;
			    int blue = (pixel) & 0xff;
			    float[] hsv = new float[3];
			    Color.RGBtoHSB(red,green,blue,hsv);
			    redImage[y*image.getWidth()+x]=red;
			    blueImage[y*image.getWidth()+x]=blue;
			    greenImage[y*image.getWidth()+x]=green;		   
			    hImage[y*image.getWidth()+x]=hsv[0];
			    sImage[y*image.getWidth()+x]=hsv[1];
			    vImage[y*image.getWidth()+x]=hsv[2];
			   
			}
		}
		
		ImageProcessor redProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), redImage);	
		ImageProcessor greenProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), greenImage);	
		ImageProcessor blueProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), blueImage);	
		ImageProcessor hProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), hImage);	
		ImageProcessor sProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), sImage);	
		ImageProcessor vProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), vImage);	
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "R", redProcessor),filterPath+"/"+FILTER_KEY+"_"+"R"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "G", greenProcessor),filterPath+"/"+FILTER_KEY+"_"+"G"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "B", blueProcessor),filterPath+"/"+FILTER_KEY+"_"+"B"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "H", hProcessor),filterPath+"/"+FILTER_KEY+"_"+"H"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "S", sProcessor),filterPath+"/"+FILTER_KEY+"_"+"S"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", vProcessor),filterPath+"/"+FILTER_KEY+"_"+"V"+".tif" );
		
	}

}
