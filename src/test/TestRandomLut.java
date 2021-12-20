package test;

import activeSegmentation.gui.RandomLUT;
import ij.IJ;
import ij.ImageJ;
import ij.process.ImageProcessor;
import ijaux.TestUtil;

public class TestRandomLut {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ImageJ();
		IJ.run("Blobs (25K)");
		ImageProcessor ip=IJ.getImage().getProcessor();
		RandomLUT lut=new RandomLUT();
		lut.getNextColor(1);
		lut.getNextColor(2);
		lut.getNextColor(3);
		lut.getNextColor(4);
		
		lut.getNextColor(8);
		lut.getNextColor(24);
		lut.getNextColor(32);
		
		TestUtil.printvector(lut.getReds());
		System.out.println("");
		TestUtil.printvector(lut.getGreens());
		System.out.println("");
		TestUtil.printvector(lut.getBlues());
		
		ip.setLut(lut.getLut());
		IJ.getImage().updateAndDraw();
		
	}

}
