package test;

public class TestHermite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("calculating hermitean coefficents: ");
		for (int i=0;i <6; i++) {
			double[]  cm=coef(i);
			System.out.print(i+" [ ");
			int k=0;
			for (double c:cm) {
				System.out.print(c+"*x^"+k+" + ");
				k++;
			}
			System.out.print(" ]\n");
			//double u=polyval2(cm, 2);
			//System.out.print(" => "+ u +"\n");
			
			
		}

		//////////////////////////////////////////////
		
		System.out.println("printing Hermitean plynomials ");
		double[][]  cm=coef3(8);		
		printcoef(cm);
		
		/////////////////////////////////////
		
		System.out.println("printing bionmial coefficients ");
		double[][]  bk=calculateBinCoefs(8);
		
		printcoef(bk);
		//////////////////////////////////////////
		
		System.out.println("calcuating Tr [n] ");
		/*
		 * Tr(n,x,y):=sum(binomial(n,i)*G(2*i,x)*G(2*n-2*i,y),i,0,n)
		 */
		calcL(bk, cm, 0, 0);
		
	} // end main


	/** calculates all powers of the Laplacean up to bk.length/2
	 * @param bk - binomial coefficients
	 * @param hk - hermite coefficients
	 */
	static void calcL(double[][] bk, double[][] hk, double x, double y) {
		int n=hk.length/2;
		for (int i=0;i <n; i++) {
			powerLap(i, bk, hk, x, y);
		} // end for
	}


	/** calculates  power of the Laplacean of order n
	 *  Tr(n,x,y):=sum(binomial(n,i)*G(2*i,x)*G(2*n-2*i,y),i,0,n)
	 * @param n
	 * @param bk
	 * @param hk
	 * @param x
	 */
	static void powerLap(int n, double[][] bk, double[][] hk, double x, double y) {
		System.out.print(n+" [ ");
		int k=0;
		double p=0;
		for (int s=0; s<bk[n].length; s++) {
			int u=2*n-2*s;
					  
			double ca=polyval2(hk[u], x);
			double cb=polyval2(hk[2*s], y);
			double b=bk[n][s];
			 
			  p+=b*ca*cb;
				 
			//System.out.print("|"+b +"|" +"*G_<"+(2*s)+">*G_<"+(2*i - 2*s)+"> + ");
			System.out.print(b +"*G_<"+(2*s)+">*G_<"+u+"> + ");
			//System.out.print("|"+ca +"|" +"*G_<"+(2*s)+">G_<"+u+">+ "); 
		 
			k++;
		}
		System.out.print(" ] => "+ p+"\n");
	}


	/**
	 * @param cm
	 */
	private static void printcoef(double[][] cm) {
		for (int i=0;i <cm.length; i++) {
			System.out.print(i+" [ ");
			int k=0;
			for (double c:cm[i]) {
				if (c!=0.0d)  {
					if (k>0)
						if (k>1)
							System.out.print(c+"*x^"+k+" + ");
						else
							System.out.print(c+"*x"+" + ");
					else
						System.out.print(c+"+ ");
				}
				k++;
			}
			System.out.print(" ]\n");
		}
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
	
	/*public static double[] coef2(int n) {

		double[][] hk=new double[n+1][n+1]; 

		hk[0][0] = 1;
		if (n>0) {
			hk[1][1]=2.0;
			for (int i=1; i<n; i++) {
				for (int k=1; k<=n; k++) {
					hk[i+1][k]= 2*(hk[i][k-1] - i* hk[i-1][k]);
				}
				hk[i+1][0]=-2*i* hk[i-1][0];
			}
		}
	
		return hk[n];
	} // end
*/	
	public static double[] coef(int n) {

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
		 
		return hk[n];
	} // end
	
	public static double[][] coef3(int n) {

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
	
	public static double polyval(double[] coef, double x) {
		
		String s="c[0]";
	 
		double ret=coef[0];
		for (int i=1; i<coef.length; i++) {
			
			double z=ret*x + coef[i];
			ret= z;
			s+="*x + c[" +i+"]";
			s="("+s+")";
			
		}
		System.out.println("["+s+"] =>" + ret);
		
		return ret;
	}
	
	public static double polyval2(double[] coef, double x) {		
			
		final int n=coef.length-1;
		double ret=coef[n];
		String s="c["+n+"]";
		for (int i=1; i<=n; i++) {			
			double z=ret*x + coef[n-i];
			ret= z;
			s+="*x + c[" +i+"]";
			s="("+s+")";			
		}
		//System.out.println("["+s+"] =>" + ret);
		
		return ret;
	}
} // end class