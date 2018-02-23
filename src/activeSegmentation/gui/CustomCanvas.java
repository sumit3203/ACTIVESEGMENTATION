package activeSegmentation.gui;

import ij.ImagePlus;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 * Custom canvas to deal with zooming an panning
 * @author Ignacio Arganda-Carreras and Johannes Schindelin
 */

public class CustomCanvas extends OverlayedImageCanvas
{
	/**
	 * default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	CustomCanvas(ImagePlus imp)
	{
		super(imp);
		Dimension dim = new Dimension(Math.min(512, imp.getWidth()), Math.min(512, imp.getHeight()));
		setMinimumSize(dim);
		setSize(dim.width, dim.height);
		setDstDimensions(dim.width, dim.height);
		addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				repaint();
			}
		});
	}
	//@Override
	public void setDrawingSize(int w, int h) {}

	public void setDstDimensions(int width, int height) {
		super.dstWidth = width;
		super.dstHeight = height;
		// adjust srcRect: can it grow/shrink?
		int w = Math.min((int)(width  / magnification), imp.getWidth());
		int h = Math.min((int)(height / magnification), imp.getHeight());
		int x = srcRect.x;
		if (x + w > imp.getWidth()) x = w - imp.getWidth();
		int y = srcRect.y;
		if (y + h > imp.getHeight()) y = h - imp.getHeight();
		srcRect.setRect(x, y, w, h);
		repaint();
	}

	//@Override
	public void paint(Graphics g) {
		Rectangle srcRect = getSrcRect();
		double mag = getMagnification();
		int dw = (int)(srcRect.width * mag);
		int dh = (int)(srcRect.height * mag);
		g.setClip(0, 0, dw, dh);

		super.paint(g);

		int w = getWidth();
		int h = getHeight();
		g.setClip(0, 0, w, h);

		// Paint away the outside
		g.setColor(getBackground());
		g.fillRect(dw, 0, w - dw, h);
		g.fillRect(0, dh, w, h - dh);
	}


}