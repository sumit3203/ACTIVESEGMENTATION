package ijaux.scale;

public class SUtils {

	private SUtils() {
		// TODO Auto-generated constructor stub
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
	 *  prints binomial coefficients
	 */
	public static void printBinCoefs(double [][] bincoefs) {
		for (int i=0; i<bincoefs.length; i++) {
			for (int j=0; j<bincoefs[i].length; j++) {
				//int a=i+1;
				//int b=j+1;
				//IJ.log("C^ ["+i + "] _["+j+"] =" +bincoefs[i][j]);
			}
		}
	}
	
	/**
	 *  implements Matlab function linear space
	 */
	public static double[] linspace(double a, double b, int N) {
		double[] ret =new double[N];
		ret[0]=a;
		ret[N-1]=b;
		double d=(b-a)/(N-1);
		for (int i=1; i<N; i++)  {
			ret[i]=ret[i-1]+d;
		}
		return ret;
	}
	
	/**
	 *  implements Matlab function linear space
	 */
	public static float[] linspace(float a, float b, int N) {
		float[] ret =new float[N];
		ret[0]=a;
		ret[N-1]=b;
		double d=(b-a)/(N-1);
		for (int i=1; i<N; i++)  {
			ret[i]=(float) (ret[i-1]+d);
		}
		return ret;
	}

	
	/** calculates the value of a polynomial assuming a_n + \sum a_{n-i} x^i
	 *  by Horners' method
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
	 *  by Horners' method
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

}