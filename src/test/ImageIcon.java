package test;

import activeSegmentation.feature.FeatureManager;
import ij.ImagePlus;
import ij.process.ByteProcessor;

public class ImageIcon {

	static ImagePlus createImageIcon(String path) {
		java.net.URL imgURL = FeatureManager.class.getClassLoader().getResource(path);
		if (imgURL != null) {
			return new ImagePlus(imgURL.getPath());
		} else {
			ByteProcessor bp=new ByteProcessor(256,256);
			return new ImagePlus("no image", bp);
		}
	}
	
	public static void main(String[] args) {
		ImagePlus icon=createImageIcon ("./no-image.jpg");
		icon.show();

	}

}
