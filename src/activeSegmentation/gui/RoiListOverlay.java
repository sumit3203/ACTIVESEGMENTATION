/**
 * Trainable_Segmentation plug-in for ImageJ and Fiji.
 * 2010 Ignacio Arganda-Carreras 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 */

package activeSegmentation.gui;

import ij.gui.Roi;
import ij.gui.ShapeRoi;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import activeSegmentation.gui.OverlayedImageCanvas.CompositeOverlay;

/**
 * This class implements an overlay based on the image ROI.
 * The overlay paints the ROI with a specific color and composite mode.
 *  
 * @author Ignacio Arganda-Carreras
 * Dimiter Prodanov
 *
 */
public class RoiListOverlay implements CompositeOverlay {
	ArrayList<Roi> roi = null;
	Color color = Roi.getColor();
	Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	
	String label="r_";
	
	/**
	 * Empty constructor
	 */
	public RoiListOverlay(){}
	
	/**
	 * Create a RoiOverlay based on a Roi and a specific color and composite mode.
	 * @param roi original image region of interest
	 * @param composite composite mode
	 * @param color color to paint the RoiOverlay
	 */
	public RoiListOverlay(ArrayList<Roi> roi, Composite composite, Color color)	{
		setRoi( roi );
		setComposite( composite );
		setColor( color );
	}
	
	public RoiListOverlay(ArrayList<Roi> roi, Composite composite, Color color, String label)	{
		this(roi, composite, color);
		setLabel(label);
	}
	
 
	
	@Override
	public void paint(Graphics g, int x, int y, double magnification) 	{
		if ( this.roi == null )
			return;
		// Set ROI image to null to avoid repainting
		int cnt=0;
		for(Roi r : this.roi)		{			
			r.setImage(null);
			Shape shape = ShapeRoiHelper.getShape(new ShapeRoi(r));
			final Rectangle roiBox = r.getBounds();
			
			final Graphics2D g2d = (Graphics2D)g;
			final Stroke originalStroke = g2d.getStroke();
			final AffineTransform ot = g2d.getTransform();
			final AffineTransform currenttransform = new AffineTransform();
			currenttransform.scale( magnification, magnification );
			currenttransform.translate( roiBox.x - x, roiBox.y - y );

			g2d.transform(currenttransform);
			
			final Composite originalComposite = g2d.getComposite();
			g2d.setComposite( this.composite );
			g2d.setColor( this.color );
	
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Font f=g.getFont();
			GlyphVector v = f.createGlyphVector(g.getFontMetrics(f).getFontRenderContext(), label+"_"+cnt);
			Shape s = v.getOutline();
			//g2d.drawString(label+"_"+cnt, x, y);
			g2d.draw(s);
			
			cnt++;
			
			final int type = r.getType();
			if( r.getStroke()!=null)
				g2d.setStroke(r.getStroke());
			
			if(type == Roi.FREELINE || type == Roi.LINE || type == Roi.POLYLINE)				
				g2d.draw(shape);							
			else
				g2d.fill(shape);
	
		
			g2d.setTransform( ot );
			g2d.setComposite(originalComposite);
			g2d.setStroke(originalStroke);
		}
				
	}
	
	public void setLabel(String lab) {
		label=lab;
	}
	
	public void setRoi(ArrayList<Roi> roi){
		this.roi = roi;
	}
	
	@Override
	public void setComposite (Composite composite)
	{this.composite = composite;}
	
	public void setColor(Color color)
	{this.color = color;}
	
	@Override
	public String toString() {
		return "RoiOverlay(" + roi + ")";
	}
	
}
