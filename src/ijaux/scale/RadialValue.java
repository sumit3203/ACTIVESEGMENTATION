package ijaux.scale;

public class RadialValue {
	double[][] Rmn;
	public RadialValue(int m,int n){
		Rmn=new double[n+1][n+1];
	}
	public double get(int m,int n){
		return Rmn[m][n];
	}
	public void set(int m, int n, double value){
		Rmn[m][n]=value;
	}
}
