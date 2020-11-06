package activeSegmentation.gui;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;

import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import static activeSegmentation.ASCommon.*;

/**
 * Extension of ImageCanvas to allow multiple overlays
 * 
 * @author Ignacio Arganda-Carreras and Johannes Schindelin
 * 
 * fix: Dimiter Prodanov
 *
 */
public class OverlayedImageCanvas extends ImageCanvas {
	
	/** Generated serial version UID */
	private static final long serialVersionUID = -9005735333215207618L;
	protected Collection<Overlay> overlays;

	private int backBufferWidth=-1;
	private int backBufferHeight=-1;

	private Graphics backBufferGraphics;
	private Image backBufferImage;
	protected Composite backBufferComposite;
	
	private double af=1.0;
	
	public OverlayedImageCanvas(ImagePlus image) {
		super(image);
		double width = image.getWidth();
		double height = image.getHeight();
		af=width/height;
		overlays = new ArrayList<>();
	}

	public void addOverlay(Overlay overlay) {
		overlays.add(overlay);
	}

	/**
	 * Add the collection of overlays to the display list of this canvas
	 * @param overlays  the overlay collection to add
	 */
	public void addOverlay(Collection<Overlay> overlays) {
		overlays.addAll(overlays);
	}

	
	public void removeOverlay(Overlay overlay) {
		overlays.remove(overlay);

	}

    /** Returns the size to which the window can be enlarged, or null if it can't be enlarged.
     *  <code>newWidth, newHeight</code> is the size needed for showing the full image
     *  at the magnification needed 
     *  
     */
    @Override
	protected Dimension canEnlarge(int newWidth, int newHeight) {

        ImageWindow win = imp.getWindow();
        final int width = imp.getWidth();
		final int height = imp.getHeight();
		
        if (win==null) return null;
        Rectangle r1 = win.getBounds();
 
        Insets insets = win.getInsets();

        r1.width = newWidth+insets.left+insets.right+ImageWindow.HGAP*2;
        r1.height = newHeight+insets.top+insets.bottom+ImageWindow.VGAP*2+win.getSliderHeight();

        Rectangle max = getMaxWindow(r1.x, r1.y);
        boolean fitsHorizontally = r1.x+r1.width<max.x+max.width;
        boolean fitsVertically = r1.y+r1.height<max.y+max.height;
        
        //System.out.println("OverlayedImageCanvas: resizing");
    	//System.out.println("newWidth: "+ newWidth+" newHeight: "+newHeight); 
 
        final int maxdim=Math.max(newWidth, newHeight);
        final int mindim=Math.max(newWidth, newHeight);
        if (maxdim>IMAGE_CANVAS_DIMENSION) {
        	System.out.println("OverlayedImageCanvas: reset (large) ");
        	return  new Dimension(IMAGE_CANVAS_DIMENSION, (int) (IMAGE_CANVAS_DIMENSION*af));
        } else if (mindim<IMAGE_CANVAS_DIMENSION/4) {
        	System.out.println("OverlayedImageCanvas: reset (small) ");
        //	resetImage( imp);
        	return  new Dimension(IMAGE_CANVAS_DIMENSION, (int) (IMAGE_CANVAS_DIMENSION*af));
        }
        
        // still to fix zoom out problems
        if (fitsHorizontally && fitsVertically) {        	
            return new Dimension(newWidth, newHeight);
        }
        /*
        else if (fitsVertically && newHeight<dstWidth) {
            return new Dimension(dstWidth, newHeight);
        }
        else if (fitsHorizontally && newWidth<dstHeight) {
            return new Dimension(newWidth, dstHeight);
        }*/
        else {
            return  new Dimension(width, height);
        }
    }

 /*
   protected void resetImage(final ImagePlus imp) {
		this.imp = imp;
		int width = imp.getWidth();
		int height = imp.getHeight();
		double ar=width/ (double) height;
		if (width>height) {
			imageWidth = IMAGE_CANVAS_DIMENSION;
			imageHeight = (int) (imageWidth/ar);
		} else {
			imageHeight = IMAGE_CANVAS_DIMENSION;
			imageWidth = (int) (imageHeight*ar);
		}
		srcRect = new Rectangle(0, 0, imageWidth, imageHeight);
		setSize(imageWidth, imageHeight);
		magnification = 1.0;
	}
   */
    
    // helper function, has only local variables
    Rectangle getMaxWindow(int xloc, int yloc) {
        Rectangle bounds = GUI.getMaxWindowBounds();
        if (xloc>bounds.x+bounds.width || yloc>bounds.y+bounds.height) {
            Rectangle bounds2 = getSecondaryMonitorBounds(xloc, yloc);
            if (bounds2!=null) return bounds2;
        }
        Dimension ijSize = ij!=null?ij.getSize():new Dimension(0,0);
        if (bounds.height>IMAGE_CANVAS_DIMENSION) {
            bounds.y += ijSize.height;
            bounds.height -= ijSize.height;
        }
        return bounds;
    }

    

    // helper function, has only local variables
    private Rectangle getSecondaryMonitorBounds(int xloc, int yloc) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Rectangle bounds = null;
        for (int j=0; j<gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i=0; i<gc.length; i++) {
                Rectangle bounds2 = gc[i].getBounds();
                if (bounds2!=null && bounds2.contains(xloc, yloc)) {
                    bounds = bounds2;
                    break;
                }
            }
        }       
        if (IJ.debugMode) IJ.log("getSecondaryMonitorBounds: "+bounds);
        return bounds;
    }
    
	/**
	 * Remove all {@link Overlay} components from this canvas.
	 */
	public void clearOverlay() {
		overlays.clear();
	}

	@Override
	public void paint(Graphics g) {
		
		if(backBufferWidth!=getSize().width ||
				backBufferHeight!=getSize().height ||
				backBufferImage==null ||
				backBufferGraphics==null)
			resetBackBuffer();
		
		final Rectangle src = getSrcRect();
		
				
		synchronized(this) {						
			super.paint(backBufferGraphics);								
			for (Overlay overlay : overlays)			
				overlay.paint(backBufferGraphics, src.x, src.y, magnification);	
			
			final Roi roi = super.imp.getRoi();
			if(roi != null)
				roi.draw(backBufferGraphics);
		}

		g.drawImage(backBufferImage,0,0,this);		
	}
	
	private void resetBackBuffer() {

		if(backBufferGraphics!=null){
			backBufferGraphics.dispose();
			backBufferGraphics=null;
		}

		if(backBufferImage!=null){
			backBufferImage.flush();
			backBufferImage=null;
		}

		backBufferWidth=getSize().width;
		backBufferHeight=getSize().height;

		backBufferImage=createImage(backBufferWidth,backBufferHeight);
	    backBufferGraphics=backBufferImage.getGraphics();

	}
	

	public interface Overlay {	
		/**
		 * Set the composite that will be used to paint this overlay.
		 */
		public void setComposite (Composite composite);
		/**
		 * Paint this overlay on the given graphic device.
		 * @param g  the graphic device provided by the {@link OverlayedImageCanvas}
		 * @param x  the top-left corner x-coordinate of the image rectangle currently displayed in the {@link OverlayedImageCanvas}
		 * @param y  the top-left corner y-coordinate  
		 * @param magnification  the {@link OverlayedImageCanvas} current magnification
		 */
		void paint(Graphics g, int x, int y, double magnification);
	}
}
