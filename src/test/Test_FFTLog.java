package test;

import static fftscale.FFTConvolver.framesize;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fftscale.ComplexFProcessor;
import fftscale.FFTConvolver;
import fftscale.filter.FFTKernelLoG;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ImageRoi;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.datatype.IComplexFArray;

public class Test_FFTLog {

	private static double sigma=3.0;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ImageJ();
		Test_FFTLog fp= new Test_FFTLog();
		fp.testFFT(fp.drawImage(512, 512));
		fp.testFFT(fp.drawImage(256, 259));
		fp.testFFT(fp.drawImage(259, 256));
		fp.testFFT(fp.drawImage(320, 245));
	}

	public  void ffttest() {
		ImagePlus image=IJ.openImage("C:\\Users\\sumit\\Documents\\demo\\test-fft\\Training\\images\\train-volume1.tif");
		image.show();
		ImageProcessor ip=image.getProcessor();
		int width=ip.getWidth();
		int height=ip.getHeight();
		testFFT(ip);
		Roi roi=new Roi(0,0, width/2+5, height/2);
		ip.setRoi(roi);
		ImageProcessor ret=(ByteProcessor) ip.crop();
		testFFT(ret);
		Roi roi1=new Roi(0,0, width/2, height/2+5);
		ip.setRoi(roi1);
		ImageProcessor ret1=(ByteProcessor) ip.crop();
		testFFT(ret1);	
	}
	public  ImageProcessor drawImage(int width, int height) {

		ImageProcessor imp = new ByteProcessor(width, height);
		imp.setColor(255);
        imp.setLineWidth(10);
		imp.drawRect(0, 0, width, height);

        imp.draw(new OvalRoi(width/4, height/4, width/2, height/2));
		//imp.drawOval(100, 100, width/2, height/2);
		//ImagePlus image=new ImagePlus("testimage", imp);
		return imp;


	}
	public  void testFFT( ImageProcessor ip) {
		//ImageProcessor ip=image.getProcessor();
		int width=ip.getWidth();
		int height=ip.getHeight();
		int[] frame=framesize(new int[]{width,height},true);
		int kw=frame[2];
		int kh=frame[3];
		System.out.println(kw);
		System.out.println(kh);
		FFTKernelLoG fgauss=new FFTKernelLoG (kw,kh, 0, sigma, true);
		FFTConvolver proc = new FFTConvolver(ip, fgauss);
		IComplexFArray kern=fgauss.getKernelComplexF();
		//ComplexFProcessor ckern=new ComplexFProcessor(kw,kh, kern);

		FloatProcessor output=proc.convolve();


		new ImagePlus("convovled",output).show();
		//new ImagePlus("kernel",ckern.stackviz()).show();
	}

}
