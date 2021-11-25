package activeSegmentation.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.Zoom;
import ij.plugin.tool.PlugInTool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import activeSegmentation.util.GuiUtil;

import java.awt.event.*;


/**
 * Based on  Custom canvas by
 * @author Ignacio Arganda-Carreras and Johannes Schindelin
 */

public class SimpleCanvas extends OverlayedImageCanvas {
	/**
	 * default serial version UID
	 */
	private static final long serialVersionUID = 1L;
		 

	public SimpleCanvas(ImagePlus imp)	{
		super(imp);
		setImage(imp);
		Dimension dim = new Dimension(Math.min(512, imp.getWidth()), Math.min(512, imp.getHeight()));
		setMinimumSize(dim);
		setSize(dim.width, dim.height);
		setDstDimensions(dim.width, dim.height);
	}
	
	public void setImage(ImagePlus imp) {
		this.imp=imp;
	}

	public void setDstDimensions(int width, int height) {
		super.dstWidth = width;
		super.dstHeight = height;
		// adjust srcRect: can it grow/shrink?
		int w = Math.min((int)(width  / magnification), getImage().getWidth());
		int h = Math.min((int)(height / magnification), getImage().getHeight());
		int x = srcRect.x;
		if (x + w > getImage().getWidth()) x = w - getImage().getWidth();
		int y = srcRect.y;
		if (y + h > getImage().getHeight()) y = h - getImage().getHeight();
		srcRect.setRect(x, y, w, h);
		 
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		Rectangle srcRect = getSrcRect();
		double mag = getMagnification();
		int dw = (int)(srcRect.width * mag);
		int dh = (int)(srcRect.height * mag);
		g.setClip(0, 0, dw, dh);

		super.paint(g);
		//this.repaintOverlay();
		int w = getWidth();
		int h = getHeight();
		g.setClip(0, 0, w, h);

		// Paint away the outside
		g.setColor(getBackground());
		g.fillRect(dw, 0, w - dw, h);
		g.fillRect(0, dh, w, h - dh);
	}

 	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		 String toolname=Toolbar.getToolName();//IJ.getToolName();
		// System.out.println(toolname);
		 if (toolname.equalsIgnoreCase("zoom")) {
			 System.out.println("zoom selected");
			 imp.killRoi();
		 }
		 super.mouseClicked(e);
		
	}

	 


	 


}