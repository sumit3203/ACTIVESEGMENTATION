/**
 * 
 */
package ijaux.scale;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * @author adminprodanov
 * 
 * Complex inline representation
 * 
 *  complex numbers encoding even positions : real part, odd position i-part
 *
 */
public class CLineIteratorIP extends IJAbstractLineIterator<float[]> {

	private ImageProcessor ip=null;
	
	public static boolean debug=false;
	
	public CLineIteratorIP(ImageProcessor ip, int xdir) {
		this.ip=ip;

		if (xdir>1) throw new IllegalArgumentException ("illegal direction "+ xdir);
		dir=xdir;
		
		if (!(ip instanceof FloatProcessor))
			throw new IllegalArgumentException ("illegal type "+ ip.getBitDepth());
			
		final int width=ip.getWidth();
		if (width%2==1)
			throw new IllegalArgumentException ("odd length");
		
		final int height=ip.getHeight();
		
		btype=32;
		initframe(width, height, dir);		
	}
	
	/**
	 * @param width
	 * @param height
	 */
	private void initframe(final int width, final int height, int dir) {
		switch (dir) {
			case Ox: {		 
				size=height; 
				blength=width;
				break;
			}
			case Oy: {
				size=width;
				blength=height<<1;
				break;
			}
		} // end 
		//if (debug)
			System.out.println("buffer length "+blength
					+" size " +size);
		initbuffer(dir);
	}
	
	/*
	 *  
	 */
	public float[] getLineFloat(ImageProcessor ip, int k, int dir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
 
		final float[] ret=(float[]) xget();
		switch (dir) {
			case Ox: {	
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (debug)
					System.out.println("fetching direction Ox " + z);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(aux , offset , ret, 0, ret.length);
						//System.out.println("offset: "+offset);
					} catch (Exception e) {
						//System.out.println("offset"+(offset % height));
						e.printStackTrace();
					}
				}
				return ret;
			}
			case Oy: {

				//final int lineno=width;
				int offset=k*height;
				k=k % (width);
				//k=k<<1;
				if (debug)
					System.out.println("fetching direction Oy "+k );
				int z=offset/(width*height);		
				if (z>=0 && z<size) {				 
					try {
						float[] pixels= (float[]) ip.getPixels();
						int sz=height<<1;
						if (pixels!=null)
							for (int y=0; y<sz; y+=2) {
								int m=y/2;
								ret[y]=pixels[k+m*width];
								ret[y+1]=pixels[k+1+m*width];	
								//System.out.print( "("+ k +" " +y +"),");
							}					
					} catch (Exception e) {
						System.out.println("k "+ k);
						e.printStackTrace();
					}
				}
				return ret;
			}			
		}
		return null;
	}
	
	public void putLine(float[] line, int k ) {
		putLineFloat(ip,line,k, dir);
	}
	
	public void putLineFloat(ImageProcessor ip, float[] line, int k, int xdir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
		
		switch (xdir) {
			case Ox: {
				if (debug)
					System.out.println("puting line in Ox : " +k);
				final int lineno=height;
				//System.out.println("lineno "+lineno);
				int offset=k*width;
				//System.out.println("offset "+offset);
				int z=offset /(width*height);
				if (z>=0 && z<lineno) {
					//System.out.print(","+z);
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(line, 0, aux, offset, width);
						//System.out.println(":"+(offset ));
						
					} catch (Exception e) {
						System.out.println("offset"+(offset));
						e.printStackTrace();
					}
				}
				break;
			}
			case Oy: {
			
				//final int lineno=width;	
				
				k=k % (width);	
				//k=k<<1;
				if (debug)
					System.out.println("puting line in Oy "+k);
				if (k>=0 && k<size) {	
					//float[] pixels= (float[]) ip.getPixels();
					try {
						
						for (int y=0; y<line.length; y+=2) {
							int m=y/2;
							ip.setf(k, m, line[y]);	
							ip.setf(k+1, m, line[y+1]);	
							// pixels[k+m*width] = line[y];
							 //pixels[k+1+m*width]=line[y+1];
							//System.out.print( "("+ k +" " +y +"),");
						}					
					} catch (Exception e) {
						System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}
	
	@Override
	public boolean isSet() {
		return !(ip==null);
	}

	@Override
	public float[] next() {
		final float[] ret=getLineFloat(ip, cnt, dir);
		//System.out.println("cnt "+cnt);
		if (dir==Ox) cnt+=1; else cnt+=2;
		return ret;
	}
	
	@Override
	public void fwd() {
		if (dir==Ox) cnt+=1; else cnt+=2;
	}
	
	@Override
	public void bck() {
		if (dir==Ox) cnt-=1; else cnt-=2;
	}
	
	@Override
	public boolean hasNext() {
		return cnt<size;
	}
	
	public void setIP(ImageProcessor ip) {	
		if (ip.getBitDepth()!=btype)
			throw new IllegalArgumentException("incompatible type " + btype);
		this.ip=ip;
		initframe(ip.getWidth(),ip.getHeight(), dir);		
	}
	
	public void reset() {
		cnt=0;
	}

}
