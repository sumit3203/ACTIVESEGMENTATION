package ijaux.scale;

import java.io.Serializable;

public class Pair<A, B> implements Serializable, Cloneable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7573914150036728958L;
	/**
	 * 
	 */
 	public A first;
    public B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    public static <A,B> Pair<A,B> of (A first, B second) {
        return new Pair<A,B>(first,second);
    }
    
    @SuppressWarnings("unchecked")
	public static <A,B> Pair<A,B> of (Pair<?,?> p) {
        return new Pair<A,B>((A)p.first,(B)p.second);
    }
    
    public static <B,A> Pair<B,A> swap (Pair<A,B> pair) {
    	return new Pair<B,A>(pair.second,pair.first);
    }
    
    public  Pair<B,A> swap() {
    	return new Pair<B,A>(second,first);
    }
    
    @SuppressWarnings("unchecked")
	public static <A,B> Pair<A,B>[] array (int n) {
        return new Pair[n];
    }
    
    @Override
    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?,?>) obj;
        if (this.first != other.first && 
                (this.first == null || !this.first.equals(other.first))) {
            return false;
        }
        if (this.second != other.second && 
                (this.second == null || !this.second.equals(other.second))) {
            return false;
        }
        return true;
    }


    @Override
    public String toString(){ 
           return "<" + first + " : " + second + ">"; 
    }
    
}
