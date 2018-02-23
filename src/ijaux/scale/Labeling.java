package ijaux.scale;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

import ij.gui.Wand;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class Labeling {
	
	public static final int l4c=0, l8c=1;
	int ltype=0;
	
	public Labeling(int type) {
		ltype=type;
	}
	
	public Polygon getContourWnd(ShortProcessor map,int label, 
			HashMap<Integer,int[]> coords, Wand wand) {
		final int width=map.getWidth();
		final int height=map.getHeight();
		int sz=width*height;
		LinkedHashMap<Integer,ArrayList<int[]>> cmap= new LinkedHashMap<Integer,ArrayList<int[]>> (100);
	
		//System.out.print("<< " +label+" \n");
		
		coords.entrySet();
		for (int i=0; i<sz; i++ ) {
			int key=map.get(i); 
			if (key==label) {
				distribute(i,   width,  cmap);
					
			} // end if
			
		} // end for
		//System.out.print("\n "+cnt+" >> \n");
		Set<Entry<Integer, ArrayList<int[]>>> entries=cmap.entrySet();
		//Iterator<Entry<Integer, ArrayList<int[]>>> iter=entries.iterator();
		int key=0;
		boolean first=false;
		for (Entry<Integer, ArrayList<int[]>> e:entries) {
			//ArrayList<int[]> value=e.getValue();
			//System.out.print(e.getKey() + ">>");
			if (!first) {
				key=e.getKey();
				break;
			}
			first=true;
			
		/*	for (int[] k: value) {
				System.out.print("("+k[0]+" "+ k[1]+"),");
			}
			System.out.print(" <<\n");
			*/
		}
		
		 		
		try {
			
			Polygon poly=new Polygon();
			ArrayList<int[]> aux=cmap.get(key);
			if (aux!=null) {
				int[] c=aux.get(0);
				int startX=c[0];
				int startY=c[1];
				//System.out.println(key +" -> ("+startX+" "+ startY+")");
				wand.autoOutline(startX, startY, label, label+1);		
				poly.xpoints=wand.xpoints;
				poly.ypoints=wand.ypoints;
				poly.npoints=wand.npoints;
				return poly;
			}  
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void doLabels(ImageProcessor bp, ShortProcessor map, int bgcol, boolean edgecorr) {
		switch (ltype) {
			case l4c: {
				doLabels4(bp, map, bgcol, edgecorr);
				}
			case l8c: {
				doLabels8(bp, map, bgcol, edgecorr);
			}
		}
	}
	
	
	final int maxd=2; // maximal metrical radius of a "circle"
	
	private void distribute(int idx, int width,  LinkedHashMap<Integer,ArrayList<int[]>> cmap) {
		int[] c=new int[2];		
		c[0]=idx % width;
		c[1]=idx / width;
		
		for (Entry<Integer, ArrayList<int[]>> e:cmap.entrySet()) {
			ArrayList<int[]> clist=e.getValue();
			final int lastind=clist.size()-1;
			final int[] ctail=clist.get(lastind);
			final int[] chead=clist.get(0);
			int dist= dist (ctail,c); 
			if (dist<maxd ) {
				clist.add(c);
				return;
			}
			dist= dist (chead,c); 
			if (dist<maxd ) {
				clist.add(0,c);
				return;
			}
			
		}
		ArrayList<int[]> alist=new ArrayList<int[]>(100);
		alist.add(c);
		cmap.put(idx, alist);
	}
	
	private int dist(int[] u, int[] v) {
		final int d= Math.max(Math.abs(u[0]-v[0]) , Math.abs(u[1]-v[1]));
		return d;
	}
	
	/** Code based on
	 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/labeldemo.htm
	 */
	public int doLabels4(ImageProcessor bp, ShortProcessor map, int bgcol, boolean edgecorr) {
		
		if (bp instanceof ColorProcessor)
			return -1;
			
		final int width=bp.getWidth();
		final int height=bp.getHeight();
		final int size=width*height;
		
		final int mwidth=map.getWidth();
		final int mheight=map.getHeight();
		
		if (width!=mwidth || height!= mheight)
			throw new IllegalArgumentException ("dimensions mismatch ");
			
		if (edgecorr) {
			for (int a=0;a<mwidth; a++) {
				bp.set(a, 0, bgcol);
			}

			for (int a=0;a<mheight; a++) {
				bp.set(0, a, bgcol);
			}
		}
		
		int [] labels  = new int[size/2];
		
		for (int i=0; i<labels.length; i++) {
			labels[i]=i ; // ramp
		}
		

		int[] nbs= new int[2];
		int[] nbslab= new int[2];
		
		int numberOfLabels =1;
		int labelColour=1; // background
		
		int result=0;
		
		for(int y=0; y<height; y++) {	 
			//labelColour=0;
			for(int x=0; x<width; x++){		
				final int val=bp.get(x, y);				
			      if( val == bgcol ){
			    	  result = 0;  //nothing here
			      } else {

			    	  //The 4 connected visited neighbours
			    	  neighborhood4(bp, nbs, x, y, width);
			    	  neighborhood4(map, nbslab, x, y, width);

			    	  //label the point
			    	 // if( (nbs[0] == nbs[1]) && (nbs[1] == nbs[2]) && (nbs[2] == nbs[3])&& (nbs[0] == bgcol )) { 
			    	  if( (nbs[0] == nbs[1])   && (nbs[0] == bgcol )) { 
					    	
			    	  // all neighbours are 0 so gives this point a new label
			    		  result = labelColour;
			    		  labelColour++;
			    	  } else { //one or more neighbours have already got labels

			    		  int count = 0;
			    		  int found = -1;
			    		  for( int j=0; j<nbs.length; j++){
			    			  if( nbs[ j ] != bgcol ){
			    				  count +=1;
			    				  found = j;
			    			  }
			    		  }
			    		  if( count == 1 ) {
			    			  // only one neighbour has a label, so assign the same label to this.
			    			  result = nbslab[ found ];
			    		  } else {
			    			  // more than 1 neighbour has a label
			    			  result = nbslab[ found ];
			    			  // Equivalence the connected points
			    			  for( int j=0; j<nbslab.length; j++){
			    				  if( ( nbslab[ j ] != 0 ) && (nbslab[ j ] != result ) ){
			    					  associate(labels, nbslab[ j ], result );
			    				  } // end if
			    			  } // end for
			    		  } // end else
			    		  
			    	  } // end else
			    	  map.set(x, y, result);
			      } // end if			    
			} // end for
		} // end for
		//reduce labels ie 76=23=22=3 -> 76=3
		//done in reverse order to preserve sorting
		System.out.println(" labels " + labelColour);
		for( int i= labels.length -1; i > 0; i-- ){
			labels[ i ] = reduce(labels, i );
		}

		/*now labels will look something like 1=1 2=2 3=2 4=2 5=5.. 76=5 77=5
			      this needs to be condensed down again, so that there is no wasted
			      space eg in the above, the labels 3 and 4 are not used instead it jumps
			      to 5.
		 */
		if (labelColour>0) {
		int condensed[] = new int[ labelColour ]; // can't be more than nextlabel labels
		
		int count = 0;
		for (int i=0; i< condensed.length; i++){
			if( i == labels[ i ] ) 
				condensed[ i ] = count++;
		}
		
		/*for( int i= condensed.length -1; i > 0; i-- ){
			System.out.println(" l " + i+ " "+condensed[ i ]);
		}*/
		numberOfLabels = count -1;
		 
		// now run back through our preliminary results, replacing the raw label
		// with the reduced and condensed one, and do the scaling and offsets too
	    for (int i=0; i< size; i++){
	    	int val=map.get(i);
	    	val = condensed[ labels[ val ] ];	        
	    	map.set(i, val);
	    }
			return numberOfLabels;
		} else {
			return -1;
		}
	}

	/** Code based on
	 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/labeldemo.htm
	 */
	public int doLabels8(ImageProcessor bp, ShortProcessor map, int bgcol, boolean edgecorr) {
		
		if (bp instanceof ColorProcessor)
			return -1;
			
		final int width=bp.getWidth();
		final int height=bp.getHeight();
		final int size=width*height;
		
		final int mwidth=map.getWidth();
		final int mheight=map.getHeight();
		
		if (width!=mwidth || height!= mheight)
			throw new IllegalArgumentException ("dimensions mismatch ");
			
		if (edgecorr) {
			for (int a=0;a<mwidth; a++) {
				bp.set(a, 0, bgcol);
			}

			for (int a=0;a<mheight; a++) {
				bp.set(0, a, bgcol);
			}
		}
		
		int [] labels  = new int[size/2];
		
		for (int i=0; i<labels.length; i++) {
			labels[i]=i ; // ramp
		}
		

		int[] nbs= new int[4];
		int[] nbslab= new int[4];
		
		int numberOfLabels =1;
		int labelColour=1; // background
		
		int result=labelColour;
		
		for(int y=0; y<height; y++) {	 
			//labelColour=0;
			for(int x=0; x<width; x++){		
				final int val=bp.get(x, y);				
			      if( val == bgcol ){
			    	  result = 0;  //nothing here
			      } else {

			    	  //The 8-connected visited neighbours
			    	  neighborhood8(bp, nbs, x, y, width);
			    	  neighborhood8(map, nbslab, x, y, width);
			    	 
			    	  //label the point
			    	  if( (nbs[0] == nbs[1]) && (nbs[1] == nbs[2])  && (nbs[2] == nbs[3]) && (nbs[0] == bgcol )) { 
			    		  // all neighbours are 0 so gives this point a new label
			    		  result = labelColour;
			    		  labelColour++;
			    	  } else { //one or more neighbours have already got labels

			    		  int count = 0;
			    		  int found = -1;
			    		  for( int j=0; j<nbs.length; j++){
			    			  if( nbs[ j ] != bgcol ){
			    				  count +=1;
			    				  found = j;
			    			  }
			    		  }
			    		  if( count == 1 ) {
			    			  // only one neighbour has a label, so assign the same label to this.
			    			  result = nbslab[ found ];
			    		  } else {
			    			  // more than 1 neighbour has a label
			    			  result = nbslab[ found ];
			    			  // Equivalence of the connected points
			    			  for( int j=0; j<nbslab.length; j++){
			    				  if( ( nbslab[ j ] != 0 ) && ( nbslab[ j ] != 0 )&& (nbslab[ j ] != result ) ){
			    					  associate(labels, nbslab[ j ], result );
			    				  } // end if
			    			  } // end for
			    		  } // end else
			    		  
			    	  } // end else
			    	  map.set(x, y, result);
			      } // end if			    
			} // end for
		} // end for
		//reduce labels ie 76=23=22=3 -> 76=3
		//done in reverse order to preserve sorting
		for( int i= labels.length -1; i > 0; i-- ){
			labels[ i ] = reduce(labels, i );
		}

		/*now labels will look something like 1=1 2=2 3=2 4=2 5=5.. 76=5 77=5
			      this needs to be condensed down again, so that there is no wasted
			      space eg in the above, the labels 3 and 4 are not used instead it jumps
			      to 5.
		 */
		if (labelColour>0) {
		int condensed[] = new int[ labelColour ]; // can't be more than nextlabel labels
		
		int count = 0;
		for (int i=0; i< labelColour; i++){
			if( i == labels[ i ] ) 
				condensed[ i ] = count++;
		}
		numberOfLabels = count-1;
		 
		// now run back through our preliminary results, replacing the raw label
		// with the reduced and condensed one, and do the scaling and offsets too
	    for (int i=0; i< size; i++){
	    	int val=map.get(i);
	    	val = condensed[ labels[ val ] ];	    
	    	//val =  labels[ val ] ;	
	    	map.set(i, val);
	 
	    }
			return numberOfLabels;
		} else {
			return -1;
		}
	}
	
	/**
	 * @param bp
	 * @param nbs
	 * @param x
	 * @param y
	 */
	public void neighborhood4(ImageProcessor bp, int[] nbs, int x, int y, int width) {
		if ( x <= 0 ) x=1;
		if ( x >= width ) x=width-1;
		if ( y <= 0 ) y=1;
		nbs[0]=bp.get(x-1,y); // west
		nbs[1]=bp.get(x,y-1); // south
	}

	/**
	 * @param bp
	 * @param nbs
	 * @param x
	 * @param y
	 */
	public void neighborhood8(ImageProcessor bp, int[] nbs, int x, int y, int width) {
		if ( x <= 0 ) x=1;
		if ( x >= width ) x=width-1;
		if ( y <= 0 ) y=1;
		nbs[0]=bp.get(x-1,y); // W
		nbs[1]=bp.get(x,y-1); // N
		nbs[2]=bp.get(x-1,y-1); // W
		nbs[3]=bp.get(x+1,y-1); // NW

	}
	
	/**
	 * @param bp
	 * @param nbs
	 * @param width
	 * @param i
	 */
	public void neighborhood4i(ImageProcessor bp, int[] nbs, int width, int i) {
		int x= i % width;
		int y =i / width;
		if ( x >= width ) x=width-1;
		if ( x <= 0 ) x=1;
		if ( y <= 0 ) y=1;
		nbs[0]=bp.get(x-1,y); // W
		nbs[1]=bp.get(x,y-1); // N
	}
	
	/**
	 * @param bp
	 * @param nbs
	 * @param width
	 * @param i
	 */
	public void neighborhood8i(ImageProcessor bp, int[] nbs, int width, int i) {
		int x= i % width;
		int y =i / width;
		if ( x <= 0 ) x=1;
		if ( y <= 0 ) y=1;
		if ( x >= width ) x=width-1;
		nbs[0]=bp.get(x-1,y); // W
		nbs[1]=bp.get(x,y-1); // N
		nbs[2]=bp.get(x-1,y-1); // NW
		nbs[3]=bp.get(x+1,y-1); // NE
	}

	 /**
	   * Associate(equivalence) a with b.
	   *  a should be less than b to give some ordering (sorting)
	   * if b is already associated with some other value, then propagate
	   * down the list.
	    */
	  private void associate(int[] labels, int a, int b ) {	    
	    if( a > b ) {
	      associate(labels, b, a );
	      return;
	    }
	    if( ( a == b ) || ( labels[ b ] == a ) ) return;
	    if( labels[ b ] == b ) {
	      labels[ b ] = a;
	    } else {
	      associate(labels, labels[ b ], a );
	      if (labels[ b ] > a) {             //***rbf new
	        labels[ b ] = a;
	      }
	    }
	  }
	  
	  /**
	   * Reduces the number of labels.
	   */
	  private int reduce(int[] labels, int a ){
	    
	    if (labels[a] == a ){
	      return a;
	    } else {
	      return reduce(labels, labels[a] );
	    }
	  }
	  
	  /*
	   *  multilevel label propagation from coarser to final spatial scale
	   */
}
