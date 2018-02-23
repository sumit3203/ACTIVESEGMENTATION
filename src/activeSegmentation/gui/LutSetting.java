package activeSegmentation.gui;

import java.awt.Color;
import java.util.List;

import ij.process.LUT;

public class LutSetting {

	LUT overlayLUT;
	// Create overlay LUT
	byte[] red = new byte[ 256 ];
	byte[] green = new byte[ 256 ];
	byte[] blue = new byte[ 256 ];

	public LUT setLut(List<Color> colors ){
		int i=0;
		for(Color color: colors){
			red[i] = (byte) color.getRed();
			green[i] = (byte) color.getGreen();
			blue[i] = (byte) color.getBlue();
			i++;
		}
		overlayLUT = new LUT(red, green, blue);
		return overlayLUT;
	}

	/**
	 * Get current label lookup table (used to color the results)
	 * @return current overlay LUT
	 */
	public LUT getOverlayLUT()
	{
		return overlayLUT;
	}
}
