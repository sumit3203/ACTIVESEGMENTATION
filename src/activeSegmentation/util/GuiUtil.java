package activeSegmentation.util;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;

public class GuiUtil {

	/**
	 * Returns a gridbag constraint with the given parameters, standard
	 * L&amp;F insets and a west anchor.
	 */
	public static GridBagConstraints getGbc(int x, int y, int width,
			boolean vFill, boolean hFill)	{
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(6, 6, 5, 5);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		if (vFill) { // position may grow vertical
			c.fill = GridBagConstraints.VERTICAL;
			c.weighty = 1.0;
		}
		if (hFill) { // position may grow horizontally
			c.fill = hFill
					? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
		}
		return c;
	}

	/*
	 * TOOD: rename to more meaningful name
	 */
	public static  JList<String> model(){
		DefaultListModel<String> traces = new DefaultListModel<String>();
		traces.addElement(" ");
		JList<String> list=new JList<String>(traces);
		list.setVisibleRowCount(5);
		list.setFixedCellHeight(20);
		list.setFixedCellWidth(100);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		return list;
	}
	
	public static  JScrollPane addScrollPanel(Component component, Dimension dimension){
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.getViewport().add( component );
		if(dimension!=null)
		scrollPane.setMinimumSize( dimension);
		return scrollPane;

	}

	public static List<Color> setDefaultColors(){
		List<Color> colors= new ArrayList<>();
		colors.add(Color.blue);
		colors.add(Color.green);
		colors.add(Color.red);
		colors.add(Color.cyan);
		colors.add(Color.magenta);
		colors.add(Color.yellow);

		return colors;
	}
	
	/**
	 * based on https://stackoverflow.com/questions/58305/
	 * is-there-a-way-to-take-a-screenshot-using-java-and-save-it-to-some-sort-of-image
	 * @param argFrame
	 */
	public static void grabWindow(ImageCanvas argFrame ) {
	    Rectangle rec = argFrame.getBounds();
	    BufferedImage bufferedImage = new BufferedImage(rec.width, rec.height, BufferedImage.TYPE_INT_ARGB);
	    argFrame.paint(bufferedImage.getGraphics());
	    ImagePlus imp=new ImagePlus("screenshot",  bufferedImage);
	    imp.show();	   
	}
}
