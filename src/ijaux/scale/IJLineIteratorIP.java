package ijaux.scale;

import ij.process.ImageProcessor;

public class IJLineIteratorIP<E> extends IJAbstractLineIterator<E> {

	private ImageProcessor ip=null;
	
	public static boolean debug=false;
	
 
	@SuppressWarnings("unchecked")
	public static  <N> IJLineIteratorIP<N> getIterator(ImageProcessor ip ,int xdir) {
		 int btype=ip.getBitDepth();		 
			if (btype==32)
				return (IJLineIteratorIP<N>) new IJLineIteratorIP<float[]>(ip,xdir);
	 		if (btype==16)
	 			return (IJLineIteratorIP<N>) new IJLineIteratorIP<short[]>(ip,xdir);
			if (btype==8)
				return (IJLineIteratorIP<N>) new IJLineIteratorIP<byte[]>(ip,xdir);
			if (btype==24)
				return (IJLineIteratorIP<N>) new IJLineIteratorIP<int[]>(ip,xdir);
			return null;
	}
	
	
	public IJLineIteratorIP(int[] dim, int bitdepth, int xdir){
		if (dim.length<2 || dim.length>5)
			throw new IllegalArgumentException("wrong dimensions number "+ dim.length);
		
		if (xdir>1) throw new IllegalArgumentException ("illegal direction "+ xdir);
		dir=xdir;	 
		btype=bitdepth;
		//System.out.println("\nbitedpth "+ btype);
		initframe(dim[0], dim[1], dir);
	}
	
	public IJLineIteratorIP(ImageProcessor ip, int xdir) {
		this.ip=ip;

		if (xdir>1) throw new IllegalArgumentException ("illegal direction "+ xdir);
		dir=xdir;

		final int width=ip.getWidth();
		final int height=ip.getHeight();
		btype=ip.getBitDepth();
		//System.out.println("\nbitedpth "+ btype);
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
				blength=height;
				break;
			}
		} // end 
		if (debug)
			System.out.println("buffer length "+blength
					+" size " +size);
		initbuffer(dir);
	}
	
	public void putLine(E line, int k){
		putLine(line,k, dir);
	}
	
	public void putLine(E line, int k, int dir) {
		if (btype==32)
			putLineFloat(ip, (float[])line, k, dir);
		if (btype==16)
			putLineShort(ip, (short[]) line, k, dir);
		if (btype==8)
			putLineByte(ip, (byte[])line, k, dir);
		if (btype==24)
			putLineInt(ip, (int[]) line, k, dir);
	}
	
	
	public void putLineInt(ImageProcessor ip, int[] line, int k, int xdir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
		
		switch (xdir) {
			case Ox: {
				if (debug)
					System.out.println("puting line in Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					if (debug)
						System.out.print(z+",");
					try {	 
						System.arraycopy(line, 0, aux, offset , width);
						//System.out.println(":"+(offset ));
					} catch (Exception e) {
						System.out.println("offset"+(offset));
						e.printStackTrace();
					}
				}
				break;
			}
			case Oy: {
				if (debug)
					System.out.println("puting line in Oy");
				final int lineno=width;				
				k=k % width;						
				if (k>=0 && k<lineno) {	
					if (debug)
						System.out.print(k+",");
					try {
						for (int y=0; y<height; y++) {
							ip.set(k, y, line[y]);		 
							//System.out.print( "("+ k +" " +y +"),");
						}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}
	
	public void putLineShort(ImageProcessor ip, short[] line, int k, int xdir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
		
		switch (xdir) {
			case Ox: {
				if (debug)
					System.out.println("puting line in Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(line, 0, aux, offset , width);
						//System.out.println(":"+(offset ));
					} catch (Exception e) {
						System.out.println("offset"+(offset));
						e.printStackTrace();
					}
				}
				break;
			}
			case Oy: {
				if (debug)
					System.out.println("puting line in Oy");
				final int lineno=width;				
				k=k % width;						
				if (k>=0 && k<lineno) {				 
					try {
						for (int y=0; y<height; y++) {
							ip.set(k, y, line[y]);		 
							//System.out.print( "("+ k +" " +y +"),");
						}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}
	
	public void putLineByte(ImageProcessor ip, byte[] line, int k, int xdir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
		
		switch (xdir) {
			case Ox: {
				if (debug)
					System.out.println("puting line in Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(line, 0, aux, offset , width);
						//System.out.println(":"+(offset ));
					} catch (Exception e) {
						System.out.println("offset"+(offset));
						e.printStackTrace();
					}
				}
				break;
			}
			case Oy: {
				if (debug)
					System.out.println("puting line in Oy");
				final int lineno=width;				
				k=k % width;						
				if (k>=0 && k<lineno) {				 
					try {
						for (int y=0; y<height; y++) {
							ip.set(k, y, line[y]);		 
							//System.out.print( "("+ k +" " +y +"),");
						}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				break;
			}
		}
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
				if (debug)
					System.out.println("puting line in Oy");
				final int lineno=width;				
				k=k % width;						
				if (k>=0 && k<lineno) {				 
					try {
						for (int y=0; y<height; y++) {
							ip.setf(k, y, line[y]);		 
							//System.out.print( "("+ k +" " +y +"),");
						}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				break;
			}
		}
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
				if (debug)
					System.out.println("fetching direction Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						//System.arraycopy(aux , offset % height, ret, 0, ret.length);
						System.arraycopy(aux , offset , ret, 0, ret.length);
						//System.out.println("offset: "+offset);
					} catch (Exception e) {
						System.out.println("offset"+(offset % height));
						e.printStackTrace();
					}
				}
				return ret;
			}
			case Oy: {
				if (debug)
					System.out.println("fetching direction Oy");
				final int lineno=width;
				int offset=k*height;
				k=k % width;
				int z=offset/(width*height);		
				if (z>=0 && z<lineno) {				 
					try {
						float[] pixels= (float[]) ip.getPixels();
						if (pixels!=null)
							for (int y=0; y<height; y++) {
								ret[y]=pixels[k+y*width];					 
								//System.out.print( "("+ k +" " +y +"),");
							}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				return ret;
			}			
		}
		return null;
	}
	
	/*
	 *  
	 */
	public short[] getLineShort(ImageProcessor ip, int k, int dir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
 
		final short[] ret=(short[]) xget();
		switch (dir) {
			case Ox: {
				if (debug)
					System.out.println("fetching direction Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(aux , offset, ret, 0, ret.length);
						//System.out.println(":"+offset/z);
					} catch (Exception e) {
						System.out.println("offset"+(offset % height));
						e.printStackTrace();
					}
				}
				return ret;
			}
			case Oy: {
				if (debug)
					System.out.println("fetching direction Oy");
				final int lineno=width;
				int offset=k*height;
				k=k % width;
				int z=offset/(width*height);		
				if (z>=0 && z<lineno) {				 
					try {
						short[] pixels= (short[]) ip.getPixels();
						if (pixels!=null)
							for (int y=0; y<height; y++) {
								ret[y]=pixels[k+y*width];					 
								//System.out.print( "("+ k +" " +y +"),");
							}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				return ret;
			}			
		}
		return null;
	}
	
	
	/*
	 *  
	 */
	public byte[] getLineByte(ImageProcessor ip, int k, int dir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
 
		final byte[] ret=(byte[]) xget();
		switch (dir) {
			case Ox: {
				if (debug)
					System.out.println("fetching direction Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(aux , offset, ret, 0, ret.length);
						//System.out.println(":"+offset/z);
					} catch (Exception e) {
						System.out.println("offset"+(offset % height));
						e.printStackTrace();
					}
				}
				return ret;
			}
			case Oy: {
				if (debug)
					System.out.println("fetching direction Oy");
				final int lineno=width;
				int offset=k*height;
				k=k % width;
				int z=offset/(width*height);		
				if (z>=0 && z<lineno) {				 
					try {
						byte[] pixels= (byte[]) ip.getPixels();
						if (pixels!=null)
							for (int y=0; y<height; y++) {
								ret[y]=pixels[k+y*width];					 
								//System.out.print( "("+ k +" " +y +"),");
							}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				return ret;
			}			
		}
		return null;
	}
	
	/*
	 *  
	 */
	public int[] getLineInt(ImageProcessor ip, int k, int dir) {
		final int width=ip.getWidth();
		final int height=ip.getHeight();
 
		final int[] ret=(int[]) xget();
		switch (dir) {
			case Ox: {
				if (debug)
					System.out.println("fetching direction Ox");
				final int lineno=height;
				int offset=k*width;
				int z=offset/(width*height);
				if (z>=0 && z<lineno) {
					Object aux=ip.getPixels();
					try {	 
						System.arraycopy(aux , offset , ret, 0, ret.length);
						//System.out.println(":"+offset/z);
					} catch (Exception e) {
						System.out.println("offset"+(offset % height));
						e.printStackTrace();
					}
				}
				return ret;
			}
			case Oy: {
				if (debug)
					System.out.println("fetching direction Oy");
				final int lineno=width;
				int offset=k*height;
				k=k % width;
				int z=offset/(width*height);		
				if (z>=0 && z<lineno) {				 
					try {
						int[] pixels= (int[]) ip.getPixels();
						if (pixels!=null)
							for (int y=0; y<height; y++) {
								ret[y]=pixels[k+y*width];					 
								//System.out.print( "("+ k +" " +y +"),");
							}					
					} catch (Exception e) {
						//System.out.println("k "+ k );
						e.printStackTrace();
					}
				}
				return ret;
			}			
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public E next() {
		Object ret=null;
		if (btype==32)
			ret=getLineFloat(  ip, cnt, dir);
 		if (btype==16)
			ret=getLineShort(  ip, cnt, dir);
		if (btype==8)
			ret=getLineByte(  ip, cnt, dir);
		if (btype==24)
			ret=getLineInt(  ip, cnt, dir); 
		cnt++;
		return (E)ret;
	}
	
	public void reset() {
		cnt=0;
	}

	public void setIP(ImageProcessor ip) {	
/*		if (ip.getBitDepth()!=btype)
			throw new IllegalArgumentException("incompatible type " + btype);
*/		btype=ip.getBitDepth();
		this.ip=ip;
		initframe(ip.getWidth(),ip.getHeight(), dir);		
	}
	
	@Override
	public boolean isSet() {
		return !(ip==null);
	}
	
	

}
