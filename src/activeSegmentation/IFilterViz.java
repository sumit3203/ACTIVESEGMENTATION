package activeSegmentation;

import java.awt.Image;

/*
 * interface for filter visualization;
 */
public interface IFilterViz {

	/**
	 *  returns the plot of the filter kernel
	 * @return Image
	 */
	Image getImage();
	
	//TODO default Image generation mechanism
	//double[] kernelData();

}