package ijaux.scale;

import java.util.Iterator;

public abstract class IJAbstractLineIterator<E> implements Iterator<E>{
	public final static int Ox=0, Oy=1, Oz=2;
	
	protected int dir=-1;
	protected int size=0;
	protected int cnt=0;
	protected int btype=-1;
	
	protected int blength=0;
	
	protected int[] intbuffer=null;
	protected byte[] bytebuffer=null;
	protected short[] shortbuffer=null;
	protected float[] floatbuffer=null;
	
	public abstract boolean isSet();
	
	protected void initbuffer(int dir) {
		switch (btype) {
			case 32: {
				floatbuffer=new float[blength];
				break;
				}
			case 16:{
				shortbuffer=new short[blength];
				break;
				}
			case 8: {
				bytebuffer=new byte[blength];
				break;
				}
			case 24: {
				intbuffer=new int[blength];
				break;
			}
			case -1:
				throw new RuntimeException("btype not initialized");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected E xget() {
		if (btype==32)
			return (E) floatbuffer;
		if (btype==16)
			return (E) shortbuffer;
		if (btype==8)
			return (E) bytebuffer;
		if (btype==24)
			return (E) intbuffer;
		return null;
	}
	
	@Override
	public boolean hasNext() {
		return cnt<size;
	}
	
	@Override
	public void remove() {
		// TODO Auto-generated method stub			
	}
	
	public int size() {
		return size;
	}
	
	public void fwd() {
		cnt++;
	}
	
	public void bck() {
		cnt--;
	}
	
	public void reset() {
		cnt=0;
	}
	
	
}