package dsp;

import ij.IJ;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.IJLineIteratorIP;
import ijaux.scale.IJLineIteratorStack;

import java.awt.Rectangle;


/**
 * @version 	1.2 23 Aug 2016
 *              1.1	14 Oct 2013
 * 				- moved contratAdjust -> Conv
 * 				- changed brightness adjustment factor to sigma^2		
 * 				1.1 	18 Jul 2013
 * 				- refactoring
 * 				1.0		05 Feb 2013 
 * 				Based on Mexican_Hat_Filter v 2.2
 * 				- common functionality is refactored in a library class
 * 				
 *   
 * 
 * @author Dimiter Prodanov IMEC , Sumit Kumar Vohra
 *
 *
 * @contents
 * This is Convolution Library that do different type of convolution
 * 
 * 
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

public class Conv {
	
	public static boolean debug=false;
	
	/**
	 * It is used for semi-separable convolution
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	public void convolveSemiSep(FloatProcessor ip, float[] kernx, float[] kern_diff) {
		FloatProcessor ip2 = null;
		FloatProcessor ipx = null;
		final Rectangle roi=ip.getRoi();
		
		synchronized(this) {		
			ip2 = (FloatProcessor)ip.duplicate();
			ip2.setRoi(roi);
			ip2.setSnapshotPixels(ip.getSnapshotPixels());
			ipx = (FloatProcessor)ip2.duplicate();
			ipx.setRoi(roi);
			ipx.setSnapshotPixels(ip.getSnapshotPixels());
		}
 
		
		
		convolveFloat1D(ipx, kern_diff, kern_diff.length, 1); // x direction
		
		
		ipx.setSnapshotPixels(null);
		convolveFloat1D(ipx, kernx, 1, kernx.length); // y direction
		
		convolveFloat1D(ip2, kernx, kernx.length, 1); // x direction
		
		ip2.setSnapshotPixels(null);
		convolveFloat1D(ip2, kern_diff, 1, kern_diff.length); // y direction
		add(ip2, ipx, ip2.getRoi());
		ip.setPixels(ip2.getPixels());
	}
	
	
	/**
	 * It is used for semi-separable convolution with 
	 * iteration rather than synchronizion
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	public void convolveSemiSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff) {
		FloatProcessor ip2 = null;
		FloatProcessor ipx = null;
		final Rectangle roi=ip.getRoi();
		
		ip2 = (FloatProcessor)ip.duplicate();
		ip2.setRoi(roi);
		ipx = (FloatProcessor)ip.duplicate();
		ipx.setRoi(roi);
		
		convolveFloat1D(ipx, kern_diff, Ox); // x direction
		//ipx.setSnapshotPixels(null);
		convolveFloat1D(ipx, kernx, Oy); // y direction
		//new ImagePlus("cx", ipx).show();
		
		convolveFloat1D(ip2, kernx, Ox); // x direction
		//ip2.setSnapshotPixels(null);
		convolveFloat1D(ip2, kern_diff, Oy); // y direction
		//new ImagePlus("cy", ip2).show();
		add(ip2, ipx, ip.getRoi());
		//new ImagePlus("roi", ip2).show();
		ip.setPixels(ip2.getPixels());
	}
	
	/**
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	public void convolveSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff) {
		convolveFloat1D(ip, kern_diff, Ox); // x direction
		//ipx.setSnapshotPixels(null);
		convolveFloat1D(ip, kernx, Oy); // y direction
		//new ImagePlus("cx", ipx).show();	
	}
	
	/**
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	public void convolveSep(ImageProcessor ip, float[] kernx, float[] kern_diff) {
		convolveFloat1D(ip, kern_diff, kern_diff.length, 1); // x direction
		//ipx.setSnapshotPixels(null);
		convolveFloat1D(ip, kernx, 1, kernx.length); // y direction
		//new ImagePlus("cx", ipx).show();	
	}
	
	/**
	 * @param ip
	 * @param kernx
	 * @param kernx
	 */
	public void convolveSemiSep(ImageStack xstack, float[] kernx, float[] kerny, float[] kernz) {
		
		long time=-System.nanoTime();
		ImageStack ystack=cloneStack(xstack);
		ImageStack zstack=cloneStack(xstack);
		
		time+=System.nanoTime();	
		time/=1000.0f;
		System.out.println("cloning time: " + time +" us");			
		time=-System.nanoTime();
	
		convolveFloat1D(xstack, kernx, Ox); // x
		convolveFloat1D(xstack, kerny, Oy); // Y
		convolveFloat1D(xstack, kernz, Oz); // Z
		
		convolveFloat1D(ystack, kernx, Oy); // Y
		convolveFloat1D(ystack, kerny, Ox); // X	
		convolveFloat1D(ystack, kernz, Oz); // Z
		
		convolveFloat1D(zstack, kernx, Oz); // Z		
		convolveFloat1D(zstack, kerny, Ox); // X
		convolveFloat1D(zstack, kernz, Oy); // Y

		
		addToStack(xstack,ystack, zstack);
		//new ImagePlus("xstack", xstack).show();
		ystack=null;
		zstack=null;
		
	
		time+=System.nanoTime();	
		time/=1000.0f;
		System.out.println("processing time: " + time +" us");
	}
	
	
	
	/**
	 * It is used to convolve 3D Stack
	 * @param xstack
	 * @param kernx
	 * @param kern_diffx
	 * @param kernz
	 */
	public void convolveSep3D(ImageStack xstack,  float[] kernx,
			float[] kern_diffx, float[] kernz) {
		 convolveFloat1D(xstack, kern_diffx, Ox);
		 convolveFloat1D(xstack, kernx, Oy);
		 convolveFloat1D(xstack, kernz, Oz);
	}

	
	private void addToStack (ImageStack dest, ImageStack a, ImageStack b) {
		int bitdepth=dest.getBitDepth();
		
		if (bitdepth!=a.getBitDepth() || a.getBitDepth()!=b.getBitDepth())
			return;
		final int sz=dest.getSize();
		for (int i=1; i<=sz; i++) {

			 switch (bitdepth) {
				 case 8: {
					 byte[] pixels= (byte[])dest.getPixels(i);
					 byte[] pixels_a= (byte[])a.getPixels(i);
					 byte[] pixels_b=(byte[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
				 case 16: {
					 short[] pixels= (short[])dest.getPixels(i);
					 short[] pixels_a= (short[])a.getPixels(i);
					 short[] pixels_b=(short[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
				 case 24: {
					 int[] pixels= (int[])dest.getPixels(i);
					 int[] pixels_a= (int[])a.getPixels(i);
					 int[] pixels_b=(int[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
				 case 32: {
					 float[] pixels= (float[])dest.getPixels(i);
					 float[] pixels_a= (float[])a.getPixels(i);
					 float[] pixels_b=(float[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
			 }
			 
			 
		 }
	}
	
	public static ImageStack cloneStack(ImageStack is) {
		final int width=is.getWidth();
		final int height=is.getHeight();
		Object[] array=is.getImageArray();
				
		ImageStack ret=ImageStack.create(width, height, array.length, is.getBitDepth());
		
		Object[] array2 =array.clone();
		int cnt=1;
		for (Object o: array2)
			ret.setPixels(o, cnt++);
		
		ret.update(is.getProcessor(1));
		ret.setRoi(is.getRoi());
		
		
		return ret;
	}
	/**
	 * @param dest
	 * @param src
	 * @param r
	 */
	private void add(ImageProcessor dest, ImageProcessor src, Rectangle r) {
		for (int y=r.y; y<r.y+r.height; y++) {
			for (int x=r.x;x<r.x+r.width; x++) {
				float sum = dest.getf(x,y) + src.getf(x,y);
				dest.setf(x, y, sum);
			}
		}
	}

	/** Convolves the float image <code>ip</code> with a kernel of width 
	<code>kw</code> and height <code>kh</code>. Returns false if 
	the user cancels the operation by pressing 'Esc'. */
	/**
	 * @param ip
	 * @param kernel
	 * @param kw
	 * @param kh
	 * @param scaled
	 * @return
	 */
	public boolean convolveFloat(ImageProcessor ip, float[] kernel, int kw, int kh) {

		int width = ip.getWidth();
		int height = ip.getHeight();
		Rectangle r = ip.getRoi();
		boolean nonRectRoi = ip.getMask()!=null;
		if (nonRectRoi)
			ip.snapshot();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = x1 + r.width;
		int y2 = y1 + r.height;
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getPixelsCopy();
		 
		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		//long lastTime = System.currentTimeMillis();
		for(int y=y1; y<y2; y++) {
			 
			for(int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				edgePixel = y<vc || y>=yedge || x<uc || x>=xedge;
				for(int v=-vc; v <= vc; v++) {
					offset = x+(y+v)*width;
					for(int u = -uc; u <= uc; u++) {
						if (edgePixel) {
							if (i>=kernel.length) // work around for JIT compiler bug on Linux
								IJ.log("kernel index error: "+i);
							sum += getPixel(x+u, y+v, pixels2, width, height)*kernel[i++];
						} else
							sum += pixels2[offset+u]*kernel[i++];
					}
				}
				pixels[x+y*width] = (float)(sum);
			}
		}
		if (nonRectRoi)
			ip.reset(ip.getMask());
		return true;
	}
	
	
	public void convolveFloat1D(FloatProcessor fp, float[] kernel, int xdir) {
		IJLineIteratorIP<float[]> iter= new IJLineIteratorIP<float[]>(fp, xdir);
		
		final int width=fp.getWidth();
		final int height=fp.getHeight();
		FloatProcessor ret=new FloatProcessor(width, height);
		
		int cnt=0;
		if (debug) {
			printvector(kernel);
			System.out.println();
		}
		while (iter.hasNext()) {
			//System.out.println(" c: "+cnt);
			final float[] line=iter.next();	
			final float[] line2=lineConvolve(line,kernel,false);
			//printvector(line2);
			//System.out.println();
			iter.putLineFloat(ret,line2, cnt, xdir);
			cnt++;		
		}
		//fp.snapshot();
		fp.setPixels(ret.getPixels());
		//new ImagePlus("cnv1", ret).show();
	}
	
	public void convolveFloat1D(ImageStack is, float[] kernel, int xdir) {
		IJLineIteratorStack<float[]> iter= new IJLineIteratorStack<float[]>(is, xdir);
		final int width=is.getWidth();
		final int height=is.getHeight();
		final int depth=is.getSize();
		ImageStack ret=ImageStack.create(width, height, depth, is.getBitDepth());
		int cnt=0;
		while (iter.hasNext()) {
			//System.out.println(" c: "+cnt);
			final float[] line=iter.next();			
			final float[] line2=lineConvolve(line,kernel,false);
			//printvector(line2);
			iter.putLineFloat(ret,line2, cnt, xdir);
			cnt++;		
		}
		
		for (int c=1; c<=depth; c++) {
			Object pixels=ret.getPixels(c);
			is.setPixels(pixels, c);
		}

		
	}
	
	static void printvector(float[] data) {
		for (int i=0; i<data.length; i++) {		
			System.out.print(data[i]+",");		
		}

	}
	
	public final static int Ox=0, Oy=1, Oz=2;
	
	
	
	/** Convolves the image <code>ip</code> with a kernel of width 
	<code>kw</code> and height <code>kh</code>. */
	/**
	 * @param ip
	 * @param kernel
	 * @param kw
	 * @param kh
	 * @param scaled
	 */
	public void convolveFloat1D(ImageProcessor ip, float[] kernel, int kw, int kh) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		Rectangle r = ip.getRoi();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = x1 + r.width;
		int y2 = y1 + r.height;
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getPixelsCopy();
 

		boolean vertical = kw==1;

		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		for(int y=y1; y<y2; y++) {
			for(int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				if (vertical) {
					edgePixel = y<vc || y>=yedge;
					offset = x+(y-vc)*width;
					for(int v=-vc; v<=vc; v++) {
						if (edgePixel)
							sum += getPixel(x+uc, y+v, pixels2, width, height)*kernel[i++];
						else
							sum += pixels2[offset+uc]*kernel[i++];
						offset += width;
					}
				} else {
					edgePixel = x<uc || x>=xedge;
					offset = x+(y-vc)*width;
					for(int u = -uc; u<=uc; u++) {
						if (edgePixel)
							sum += getPixel(x+u, y+vc, pixels2, width, height)*kernel[i++];
						else
							sum += pixels2[offset+u]*kernel[i++];
					}
				}
				pixels[x+y*width] = (float)(sum);
			}
		}
	}
	
	/**
	 * @param kernel
	 */
	public static void flip(float[] kernel) {
		final int s=kernel.length-1;
		for (int i=0; i< kernel.length/2; i++) {
			final float c=kernel[i];
			kernel[i]=kernel[s-i];
			kernel[s-i]=c;
		}
	}
	
	/* 
	 * computes correlation operation between arrays
	 */
	public static float[] lineConvolve(float[] arr, float[] kernel, boolean flip) {
		if (flip)
			flip(kernel);
		
	
		float[] y= new float[arr.length];
		
		int kw=kernel.length/2;
		//System.out.println("pre loop < "+kw);
		for (int i=0; i<kw; i++) {
			int c=0;
			for (int k=-kw; k<=kw; k++) {
				int q=i-k;
				if (0<=q && q < arr.length) {
					y[i]+=arr[q]*kernel[c];
					c++;
				}
				else{
					
					y[i]+=arr[0]*kernel[c];
					c++;
				}
		
			}			
		}
		// main loop	    
	    for (int i=kw;i<arr.length-kw;i++) {	    	
	        int c=0;
			for (int k=-kw; k<=kw; k++) {
				y[i]+=arr[i-k]*kernel[c];
				c++;
			}
	    }
	    
		//System.out.println("post loop => "+(arr.length-kw));
		for (int i=arr.length-kw; i<arr.length; i++) {
			int c=0;
			for (int k=-kw; k<=kw; k++) {
				int q=i-k;
				if (q < arr.length && 0<=q ) {
					y[i]+=arr[q]*kernel[c];
					c++;
				}
				else {
					y[i]+=arr[arr.length-1]*kernel[c];
					c++;
				}
			}
			
		}
	    return y;
	}
	
	
	 
	private float getPixel(int x, int y, float[] pixels, int width, int height) {
		if (x<=0) x = 0;
		if (x>=width) x = width-1;
		if (y<=0) y = 0;
		if (y>=height) y = height-1;
		return pixels[x+y*width];
	}

	/**
	 * @param fpaux
	 * @param dr
	 * @param d1
	 */
	public static void contrastAdjust(FloatProcessor fpaux, double dr, final double d1) {
		float[] pixels=(float[]) fpaux.getPixels();
		int width=fpaux.getWidth();
		Rectangle rect=fpaux.getRoi();
		for (int i=0; i < pixels.length; i++) {
			final int x=i % width;
			final int y=i / width;
			if (rect.contains(x, y)) {
			pixels[i]= (float) (pixels[i]*dr+d1);
			}
		}
	}
	
	/**
	 * 
	 * @param fp
	 * @return  
	 */
	public static float[] findMinAndMax(FloatProcessor fp) {
		float[] pixels=(float[]) fp.getPixels();
		int width=fp.getWidth();
	 	Rectangle rect=fp.getRoi();
		float min = pixels[0];
		float max =  min;
		for (int i=0; i < pixels.length; i++) {
			final int x=i % width;
			final int y=i / width;
			if (rect.contains(x, y)) {
				float value = pixels[i];
				if (!Float.isInfinite(value)) {
					if (value<min)
						min = value;
					if (value>max)
						max = value;
				}
			}
		}
		
		System.out.println("min " +min +" max " + max);
		
		return new float[]{min,max}; 
	}
	
}
