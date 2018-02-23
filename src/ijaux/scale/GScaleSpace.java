package ijaux.scale;

import ij.IJ;
import ij.ImageStack;
import ij.process.FloatProcessor;
import static java.lang.Math.*;

/*
* @version 	    1.2.1 21 March 2014
* 			
* 				1.2 20 Oct 2013
* 				- fixed a bug in the computation of computeLapNKernel2D
* 				- added double precision computations
* 				- added power of the Laplacian (poweLap) computation
* 				1.1.6
* 				- refactoring getSigma -> getS
* 				1.1.5
* 				- regression to hw=3.0*sigma
* 				1.1 17 Jul 2013
* 				- sampling computation change hw=4.0*sigma
* 				- code refactoring, parameter name change
* 				- added getSigma
* 
* 				1.0	5 Feb 2013
*   
* 
* @author Dimiter Prodanov
* 		  IMEC
* 
* @contributors:  Dimiter Prodanov, Tomasz Konopczynski
* 
*  This library implements Gaussian scale space functionality
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

public class GScaleSpace {
	
	public static final String version="1.2";	
	
	
	public static boolean debug=IJ.debugMode;	
	
	protected int r=1;	
	
	protected float sigma=-1.0f;
	
	private static float swidth=3.0f; // default sampling radius

	/*
	 * CONSTRUCTORS
	 */
	
	/**
	 * @param s - sigma
	 * @param width - window width in sigma units,  min 3sigma
	 * we sample the interval [- width*sigma + width*sigma]
	 * */
	
	public GScaleSpace (float sigma) {
		r=(int)(swidth*sigma);
		
			System.out.println("r "+ r +" sigma "+ sigma);
		
		this.sigma=sigma;
	}
	
	/**
	 * @param s - sigma
	 * @param width - window width in sigma units,  min 3sigma
	 * we sample the interval [- width*sigma + width*sigma]
	 * */
	
	public GScaleSpace (float sigma, float width) {
		if (width<3.0f)
			width=3.0f;
		swidth=width; // we sample at least 3* sigma
		r=(int)(swidth*sigma);
		if (debug)
			System.out.println("r "+ r +" sigma "+ sigma);
		
		this.sigma=sigma;
	}
	
	/**
	 * @param hw - sampling window 1/2-width
	 */
	
	public GScaleSpace (int hw) {
		r=hw;
		float sigma=((float)(2*hw+1))/(2*swidth);
		if (debug)
			System.out.println("r: "+hw+" sigma "+sigma);
		this.sigma=sigma;
	}
	
	/**
	 * @param hw - kernel 1/2-width
	 * @param sk - sampling coefficient
	 * kept for compatibility reasons
	 */
	
	public GScaleSpace (int hw, float sk) {
		r=hw;
		GScaleSpace.swidth=sk;
		float sigma=((float)(2*hw+1))/(2*sk);
		if (debug)
			System.out.println("r: "+hw+" sigma "+sigma);
		this.sigma=sigma;
	}
	
//////////////////////////////////////////////////////////
	/*
	 * Methods
	 */
/////////////////////////////////////////////////////////
	
	public static void scnorm(float[] kern, double sigma, int n) {
		sigma=Math.pow(sigma, n);
		for (int i=0; i<kern.length; i++) {
			kern[i]*=sigma;
		}
	}
	
	public int getSize() {
		return (2*r+1);
	}

	/*
	 *  returns the sigma parameter e.g. the standard deviation in the Gaussian
	 */
	public double getSigma() {
		return sigma;
	}
	
	/*
	 *  returns the scale e.g. the variation in the Gaussian
	 */
	public double getScale() {
		return sigma*sigma;
	}
	/**
	 * @return
	 */
	public float[] gauss1D() {
		return gauss1D(r) ;
	}
	
	/**
	 * @param r
	 * @return
	 */
	// KONOP'S
	public static float[] gauss1D(float width, float sigma) {
		// for the others: :
		//double sigma2= ((double)sz)/(6);
		//sigma2 = r/swidth;
		//sigma2*=sigma2;
		//swidth=width; // we sample at least 3* sigma
		double sigma2= (double) sigma;
		int r=(int)(width*sigma2+1);
		System.out.print("r: "+r);
		int sz=2*r+1;
		sigma2*=sigma2;
		System.out.println("sigma**2: "+sigma2);
		
	 	
		float[] kernel=new float[sz];

		final double PIs=1/Math.sqrt(2*PI*sigma2);
		if (debug)  
			System.out.print(" \n");
		 
		for (int u=-r; u<=r; u++) {
		 	
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(float)(Math.exp(-0.5*x2/sigma2)*PIs);
			if (debug) 
				System.out.print(kernel[idx] +" ");
		}
		if (debug)  
			System.out.print(" \n");
		 
		
		return kernel;
	}
	
	public static float[] gauss1D(int r) {
		int sz=2*r+1;
		
		double sigma2=((double)sz)/(2*swidth);
		sigma2 = r/swidth;
		//sigma2*=sigma2;
		
	 	
		float[] kernel=new float[sz];

		final double PIs=1/Math.sqrt(2*PI*sigma2);
		if (debug)  
			System.out.print(" \n");
		 
		for (int u=-r; u<=r; u++) {
		 	
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(float)(Math.exp(-0.5*x2/sigma2)*PIs);
			if (debug) 
				System.out.print(kernel[idx] +" ");
		}
		if (debug)  
			System.out.print(" \n");
		 
		
		return kernel;
	}
	
	/**
	 * @param r
	 * @return
	 */
	public static double[] gauss1Dd(int r) {
		int sz=2*r+1;
		
		double sigma2=((double)sz)/(2*swidth);
		sigma2 = r/swidth;
		//sigma2*=sigma2;
	 	
		double[] kernel=new double[sz];

		final double PIs=1/Math.sqrt(2*PI*sigma2);
		if (debug)  
			System.out.print(" \n");
		 
		for (int u=-r; u<=r; u++) {
		 	
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]= Math.exp(-0.5*x2/sigma2)*PIs;
			if (debug) 
				System.out.print(kernel[idx] +" ");
			 
		}
		if (debug)  
			System.out.print(" \n");
		 
		
		return kernel;
	}

	/**
	 * @return
	 */
	public float[] gauss2D() {
		return gauss2D(r);
	}
	
	/**
	 * @param r
	 * @return
	 */
	public static float[] gauss2D(int r) {
		int sz=2*r+1;
		
		//double sigma2=((double)r/3.0+1/6.0);
		double sigma2=((double)sz)/(2*swidth);
		sigma2*=sigma2;
		
		float[] kernel=new float[sz*sz];
 
		final double PIs=0.5/PI/sigma2;

		for (int u=-r; u<=r; u++) {
			if (debug)
				System.out.print("\n ");	
			for (int w=-r; w<=r; w++) {
				final double x2=u*u+w*w;
				final int idx=sz*(u+r) + (w+r);
				kernel[idx]=(float)(exp(-0.5*x2/sigma2)*PIs);
				if (debug)
					System.out.print(kernel[idx] +" ");		
			}
		}
		if (debug)
			System.out.print("\n ");	
		
		return kernel;
	}
	
	/**
	 * @return
	 */
	public float[]  diffGauss1D() {
		return diffGauss1D(r);
	}
	
	
	/**
	 * @param r
	 * @return
	 */
	public static float[]  diffGauss1D(int r) {
		int sz=2*r+1;

		//double sigma2=((double)r/3.0+1/6.0);
		double sigma=((double)sz)/(2*swidth);
		
		double s=sigma*sigma;
		
		System.out.println("s^2: "+s);
		System.out.println("width: "+swidth);
		float[] kernel=new float[sz];
  
		final double PIs=1/sqrt(2*Math.PI*s)/s;
		for (int u=-r; u<=r; u++) {
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(float)(u*exp(-0.5*x2/s)*PIs);
		 
		}
		 
		return kernel;
	}

	//konop's
	public static float[]  diffGauss1D(float width, float sigma) {
		
		System.out.println("Width :"+width);
		System.out.println("Sigma :"+sigma);
		//swidth=width; // we sample at least 3* sigma
		double s= (double) sigma;
		int r=(int)(width*s);
		int sz=2*r+1;
		s=sigma*sigma;
		
		float[] kernel=new float[sz];
  
		final double PIs=1/sqrt(2*Math.PI*s)/s;
		for (int u=-r; u<=r; u++) {
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(float)(u*exp(-0.5*x2/s)*PIs);
		 
		}
		 
		return kernel;
	}
	/**
	 * @param r
	 * @return
	 */
	public static double[]  diffGauss1Dd(int r) {
		int sz=2*r+1;

		//double sigma2=((double)r/3.0+1/6.0);
		double sigma=((double)sz)/(2*swidth);
		
		double s=sigma*sigma;
		
		double[] kernel=new double[sz];
  
		final double PIs=1/Math.sqrt(2*PI*s)/s;
		for (int u=-r; u<=r; u++) {
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(u*Math.exp(-0.5*x2/s)*PIs);
		 
		}
		 
		return kernel;
	}
	
	/**
	 * @return
	 */
	public float[] diff2Gauss1D() {
		return diff2Gauss1D(r);
	}
			
	/**
	 * @param r
	 * @return
	 */
	public static float[] diff2Gauss1D(int r) {
		int sz=2*r+1;
		
		//double sigma2=((double)r/3.0+1/6.0);
		double sigma2=((double)sz)/(2*swidth);
		sigma2*=sigma2;
		
		float[] kernel=new float[sz];
 
		final double PIs=1/Math.sqrt(2*PI*sigma2)/sigma2/sigma2;
		if (debug)  
			System.out.print(" \n");
	 
		for (int u=-r; u<=r; u++) {
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(float)((x2-sigma2)*exp(-0.5*x2/sigma2)*PIs);
			if (debug)  
				System.out.print(kernel[idx] +" ");
			 
		}
		
		if (debug)  
			System.out.print(" \n");
		 

		return kernel;
	}

	//konops
	public static float[] diff2Gauss1D(float width, double sigma2) {
		
		//swidth=width; // we sample at least 3* sigma
		int r=(int)(width*Math.sqrt(sigma2));
		//System.out.print("r: "+r);
		IJ.log("R:"+r);
		int sz=2*r+1;
				
		float[] kernel=new float[sz];
 
		final double PIs=1/Math.sqrt(2*PI*sigma2)/sigma2/sigma2;
		if (debug)  
			System.out.print(" \n");
	 
		for (int u=-r; u<=r; u++) {
			final double x2=u*u;
			final int idx=u+r ;
			kernel[idx]=(float)((x2-sigma2)*exp(-0.5*x2/sigma2)*PIs);
			if (debug)  
				System.out.print(kernel[idx] +" ");
		}
		if (debug)  
			System.out.print(" \n");
		return kernel;
	}
	/**
	 * @param n
	 * @return
	 */
	public float[] diffNGauss1D( int n) {
		return diffNGauss1D(r,n);
	}
	
	/**
	 * @param n
	 * @return
	 */
	public double[] diffNGauss1Dd( int n) {
		return diffNGauss1Dd(r,n);
	}
	
	// KONOPS
	public static float[] diffNGauss1D(float width,float sigma,int n) {
		//debug=true;
		double[] hk = hermiteCoef(n)[n];
		if (debug) {
			System.out.println("hermite coefficients ");
			for (double c:hk) {
				System.out.print(c+" ");
			}
			System.out.println("");
		}	
		
		//swidth=width; // we sample at least 3* sigma
		double s= (double) sigma;
		int r=(int)(width*s);
		int sz=2*r+1;
		s=sigma*sigma;
		
		//int sz=2*r+1;
		
		//final double sigma2=((double)r/3.0+1/6)*((double)r/3.0 +1/6.0);
		//double sigma=((double)sz)/(2*swidth);
		//double s=sigma*sigma;
		float[] kernel=new float[sz];
	 
		int z= (n & 1);
		if (debug)  
			System.out.println("z "+z);
		
		double PIs=1/Math.sqrt(2.0*PI*s)/pow(s, ((double)n)/2.0);
		if (z>0) {
			PIs=-PIs;
		}
		if (debug)
			System.out.print(PIs+" Gaussian derivative order "+ n+" \n");
		
		for (int u=-r; u<=r; u++) {
			double x=-u/sigma;
			x=polyval2(hk,x);
			final int idx=u+r ;
			kernel[idx]=(float)(x*exp(-0.5*u*u/s)*PIs);
			
			if (debug)
				System.out.print(kernel[idx] +" ");
		}
		if (debug)
			System.out.print(" \n");
	 
		return kernel;
	}
	
	
	/**
	 * @param r
	 * @param n
	 * @return
	 *  G(n,x):=(-1)^n*s^((-n)/2)*he(n,x/sqrt(s))*g(x)
	 *  where s=sigma^2
	 */
	public float[] diffNGauss1D(int r,int n) {
		//debug=true;
		double[] hk = hermiteCoef(n)[n];
		if (debug) {
			System.out.println("hermite coefficients ");
			for (double c:hk) {
				System.out.print(c+" ");
			}
			System.out.println("");
		}	
		int sz=2*r+1;
		
		//final double sigma2=((double)r/3.0+1/6)*((double)r/3.0 +1/6.0);
		double sigma=((double)sz)/(2*swidth);
		double s=sigma*sigma;
		float[] kernel=new float[sz];
	 
		int z= (n & 1);
		if (debug)  
			System.out.println("z "+z);
		
		double PIs=1/Math.sqrt(2.0*PI*s)/pow(s, ((double)n)/2.0);
		if (z>0) {
			PIs=-PIs;
		}
		if (debug)
			System.out.print(PIs+" Gaussian derivative order "+ n+" \n");
		
		for (int u=-r; u<=r; u++) {
			double x=-u/sigma;
			x=polyval2(hk,x);
			final int idx=u+r ;
			kernel[idx]=(float)(x*exp(-0.5*u*u/s)*PIs);
			
			if (debug)
				System.out.print(kernel[idx] +" ");
		}
		if (debug)
			System.out.print(" \n");
	 
		return kernel;
	}
	 
	/**
	 * @param r
	 * @param n
	 * @return
	 *  G(n,x):=(-1)^n*s^((-n)/2)*he(n,x/sqrt(s))*g(x)
	 *  where s=sigma^2
	 */
	public double[] diffNGauss1Dd(int r,int n) {
		//debug=true;
		double[] hk = hermiteCoef(n)[n];
		if (debug) {
			System.out.println("hermite coefficients ");
			for (double c:hk) {
				System.out.print(c+" ");
			}
			System.out.println("");
		}	
		int sz=2*r+1;
		
		//final double sigma2=((double)r/3.0+1/6)*((double)r/3.0 +1/6.0);
		double sigma=((double)sz)/(2*swidth);
		double s=sigma*sigma;
		double[] kernel=new double[sz];
	 
		int z= (n & 1);
		if (debug)  
			System.out.println("z "+z);
		
		double PIs=1/Math.sqrt(2.0*PI*s)/pow(s, ((double)n)/2.0);
		if (z>0) {
			PIs=-PIs;
		}
		if (debug)
			System.out.print(PIs+" Gaussian derivative order "+ n+" \n");
		
		for (int u=-r; u<=r; u++) {
			double x=-u/sigma;
			x=polyval2(hk,x);
			final int idx=u+r ;
			kernel[idx]=(x*exp(-0.5*u*u/s)*PIs);
			
			if (debug)
				System.out.print(kernel[idx] +" ");
		}
		if (debug)
			System.out.print(" \n");
	 
		return kernel;
	}
	
	/**
	 * @return
	 */
	public float[] computeDiff2Kernel2D( ) {
		return computeDiff2Kernel2D(r);
	}
	
	/**
	 * @param r
	 */
	public static float[] computeDiff2Kernel2D(int r) {
		int sz=2*r+1;

		//final double sigma2=((double)r/3.0+1/6)*((double)r/3.0 +1/6.0);
		double sigma2=((double)sz)/(2*swidth);
		sigma2*=sigma2;
		
		float[] kernel=new float[sz*sz];
 
		final double PIs=0.5/Math.PI/sigma2/sigma2/sigma2;

		for (int u=-r; u<=r; u++) {
			if (debug)
				System.out.print("\n ");	
			for (int w=-r; w<=r; w++) {
				final double x2=u*u+w*w;
				final int idx=sz*(u+r) + (w+r);
				kernel[idx]=(float)((x2 -2*sigma2)*Math.exp(-0.5*x2/sigma2)*PIs);
				if (debug)
					System.out.print(kernel[idx] +" ");		
			}
		}
		if (debug)
			System.out.print("\n ");	
		
		return kernel;
	}
	
	/**
	 * @param n
	 * @return
	 */
	public float[] computeLapNKernel2D( int n) {
		return computeLapNKernel2D(r,n);
	}
	
	/**
	 * @param r
	 * 
	 * Lr(n,x,y):=sum(binomial(n,i)*G(2*i,x)*G(2*n-2*i,y),i,0,n)
	 * where
	 * G(n,x):=(-1)^n*s^((-n)/2)*He(n,x/sqrt(s))*g(x)
	 * where 
	 * He(n,x) is a Hermite polynomial of order n
	 * 
	 */
	public float[] computeLapNKernel2D(int r, int n) {
		double[][] bincoef = calculateBinCoefs(2*n);
		double[][] hercoef = hermiteCoef(2*n);
		
		int sz=2*r+1;
		//final double sigma2=((double)r/3.0+1/6)*((double)r/3.0 +1/6.0);
		double sigma=((double)sz)/(2*swidth);
		double sigma2=sigma*sigma;
		
		float[] kernel=new float[sz*sz];
	
		//int z= (n & 1);
		double PIs=0.5/Math.PI/sigma2/Math.pow(sigma2, n);

		if (debug)
			System.out.println(PIs  +" Gaussian derivative kernel 2D order " +n);	
	 
		for (int u=-r; u<=r; u++) {
			//System.out.print("\n ");	
			for (int w=-r; w<=r; w++) {
				final double r2=u*u+w*w;
				double x=u/sigma;				
				double y=w/sigma;
	
				double p=powerLap(n, bincoef, hercoef, x,y);
				
				final int idx=sz*(u+r) + (w+r);
				kernel[idx]=(float)(p*Math.exp(-0.5*r2/sigma2)*PIs);
				if (debug)
					System.out.print(kernel[idx] +" ");		
			
			}
		}
		if (debug)
			System.out.print("\n ");	

		return kernel;
	}
	
	
	/** calculates  power of the Laplacean of order n
	 *  Tr(n,i,x,y):=binomial(n,i)*hk(2*i,x)*hk(2*n-2*i,y)
	 * @param n
	 * @param bk
	 * @param hk
	 * @param x
	 */
	public double powerLap(int n, double[][] bk, double[][] hk, double x, double y) {
		//System.out.print(n+" [ ");
		//int k=0;
		double p=0;
		for (int s=0; s<bk[n].length; s++) {
			int u=2*n-2*s;					  
			double ca=polyval2(hk[u], x);
			double cb=polyval2(hk[2*s], y);
			double b=bk[n][s];			 
			p+=b*ca*cb;				 
			//System.out.print(b +"*G_<"+(2*s)+">*G_<"+u+"> + ");
		//k++;
		}
		return p;
		//System.out.print(" ] => "+ p+"\n");
	}
	
	public static double[][]  calculateBinCoefs( int n) {
		double[][] carray=new double[n][];
		//IJ.log("length 1: "+carray.length);
		for (int z=1; z<=n; z++){
			carray[z-1]=new double [z];
			//IJ.log("length 2: "+ carray[z-1].length);
			for (int k=1; k<=z; k++) {
				if ((k==z) || (k<=1) ){
					carray[z-1][k-1]=1;
				} else {
					carray[z-1][k-1]=carray[z-2][k-2]+ carray[z-2][k-1];
					//IJ.log("c ["+z + "]["+k +"] =" +carray[z-1][k-1]);
				}
			}
		}	
		return carray;	 
	}
	
	 
	
	public static void printBinCoefs(double [][] bincoefs) {
		for (int i=0; i<bincoefs.length; i++) {
			for (int j=0; j<bincoefs[i].length; j++) {
				//int a=i+1;
				//int b=j+1;
				IJ.log("C^ ["+i + "] _["+j+"] =" +bincoefs[i][j]);
			}
		}
	}
	
	/**
	 * @param n
	 * @return
	 */
	public static double[][] hermiteCoef(int n) {

		double[][] hk=new double[n+1][n+1]; 

		hk[0][0] = 1;
		if (n>0) {
			hk[1][1]=1.0;
			for (int i=1; i<n; i++) {				
				for (int k=1; k<=n; k++) {
					hk[i+1][k]= (hk[i][k-1] - i* hk[i-1][k]);
				}
				hk[i+1][0]=-i*hk[i-1][0];
			}
		}
		double[][] ret=new double[n+1][];
		for (int i=0; i<=n; i++) {	
			 ret[i]= new double[i+1];
			 for (int j=0; j<=i; j++)
				 ret[i][j]=hk[i][j];
		}
		return ret;
	} // end

	/** calculates the value of a polynomial assuming a_n + \sum a_{n-i} x^i
	 * @param coef
	 * @param x
	 * @return
	 */
	public static double polyval(double[] coef, double x) {		
		final int n=coef.length-1;
		double ret=coef[0];
		for (int i=1; i<=n; i++) {			
			double z=ret*x + coef[i];
			ret= z;	
		}	
		return ret;
	}
	
	/** calculates the value of a polynomial assuming a_0 + \sum a_i x^i
	 * @param coef
	 * @param x
	 * @return
	 */
	public static double polyval2(double[] coef, double x) {			
		final int n=coef.length-1;
		double ret=coef[n];
		for (int i=1; i<=n; i++) {			
			double z=ret*x + coef[n-i];
			ret= z;			
		}
		return ret;
	}
	
	
	public static ImageStack join(FloatProcessor fp1, FloatProcessor fp2) {
		int width=fp1.getWidth();
		int height=fp1.getHeight();		
		//int depth=fp2.getWidth();
		//int depth=fp1.getHeight();
		if (width!=fp2.getWidth())
			throw new IllegalArgumentException("size mismatch");
		ImageStack is=new ImageStack(width, height);
	 	
		IJLineIteratorIP<float[]> iter= 
				new IJLineIteratorIP<float[]>(fp2, 0); // Ox
				
				//int cnt =0; 
		while (iter.hasNext()) {
			//System.out.println("cnt "+cnt);
			float[] coly=iter.next();
			FloatProcessor aux=(FloatProcessor) fp1.duplicate();	
			for (int y=0; y<height; y++) {		
				for (int x=0; x<width; x++) {
					float value=aux.getf(x,y)*coly[x];
					aux.setf(x, y, value);				 
				}
			}

			is.addSlice(aux);
			//cnt++;
		}

		return is;
	}
	
	public static ImageStack join(FloatProcessor fp1, float[] colz) {
		int width=fp1.getWidth();
		int height=fp1.getHeight();		
 
		ImageStack is=new ImageStack(width, height);

		for (int z=0; z<colz.length; z++) {		 
			FloatProcessor aux=(FloatProcessor) fp1.duplicate();	
			for (int y=0; y<height; y++) {		
				for (int x=0; x<width; x++) {
					float value=aux.getf(x,y)*colz[z];
					aux.setf(x, y, value);				 
				}
			}

			is.addSlice(aux);
 
		}

		return is;
	}
	/**
	 * @param kernel
	 * @param a
	 * @param b
	 * @return
	 */
	public static float[] joinXY(float[][] kernel, int a, int b) {

		int wa=kernel[a].length; // cols
		int wb=kernel[b].length; // rows
	
		float[] jkernel=new float[wa*wb];

		for (int i=0; i<jkernel.length; i++) {
			jkernel[i]=1.0f;
		}
		if (a>=0) { // columns
			final float[] col=kernel[a]; // ->wa
			for (int c=0; c<wa; c++) { // col
				for (int r=0; r<wb; r++) { // row							
					final int idx=c + r*wa;
					jkernel[idx]*=col[c];
				}
			}
		}
		if (b>=0) { // rows
			final float[] row=kernel[b]; // ->wb
			for (int r=0; r<wb; r++) { // row	
				for (int c=0; c<wa; c++) { // col					
					final int idx=c + r *wa;
					jkernel[idx]*=row[r];
				}
			}
		}
		return jkernel;

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
	
	/**
	 * @param kernel
	 */
	public static void flip(double[] kernel) {
		final int s=kernel.length-1;
		for (int i=0; i< kernel.length/2; i++) {
			final double c=kernel[i];
			kernel[i]=kernel[s-i];
			kernel[s-i]=c;
		}
	}
	
	/**
	 * @param kernel
	 */
	public static void transp(double[] kernel, int sz) {
		if (kernel.length/sz!=sz) 
			return; // not a square kernel		

		for (int row=0; row< sz; row++) {
			for (int col=0; col< sz; col++) {
				final int i=row*sz+col;
				final double c=kernel[i];
				final int j=row+col*sz;
				kernel[i]=kernel[j];
				kernel[j]=c;
			}
		}
	}
	
	/**
	 * @param kernel
	 */
	public static void transp(float[] kernel, int sz) {
		if (kernel.length/sz!=sz) 
			return; // not a square kernel		

		for (int row=0; row< sz; row++) {
			for (int col=0; col< sz; col++) {
				final int i=row*sz+col;
				final float c=kernel[i];
				final int j=row+col*sz;
				kernel[i]=kernel[j];
				kernel[j]=c;
			}
		}
	}
}
