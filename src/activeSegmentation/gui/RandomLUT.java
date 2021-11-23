package activeSegmentation.gui;

import java.awt.Color;
import java.awt.image.*;
import java.util.List;
import java.util.Random;

import activeSegmentation.util.GuiUtil;
import ij.LookUpTable;
import ij.process.LUT;

public class RandomLUT extends LookUpTable {
	 
	private List<Color> defaultColors = GuiUtil.setDefaultColors();
	private Random rand = new Random();
	
	private int counter=0;
	
	public RandomLUT() {
		this(LookUpTable.createGrayscaleColorModel(false));
	}
	
	public RandomLUT(ColorModel cm) {
		super(cm);
		counter=defaultColors.size();
	}
	
	
	public Color getNextColor(int number) {
		number=number % 256;
		if (number < defaultColors.size() ) {
			Color c=defaultColors.get(number);
			int val=c.getRGB();
			int r = (val&0xff0000)>>16;
			int g = (val&0xff00)>>8;
			int b = val&0xff;
			getReds()[number]=(byte) (r);
			getGreens()[number]=(byte) (g);
			getBlues()[number]=(byte) (b);	
			counter=number;
			return c;
		} else {
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			Color randomColor = new Color(r, g, b);
			getReds()[number]=(byte) (255*r);
			getGreens()[number]=(byte) (255*g);
			getBlues()[number]=(byte) (255*b);
			counter=number;
			return randomColor;
		}

	}
	
	public LUT getLut() {
		return new LUT((IndexColorModel)getColorModel(), 0.0, 255.0) ;
	}
	
	
}