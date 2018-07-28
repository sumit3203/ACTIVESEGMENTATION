package test;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ijaux.scale.GScaleSpace;



public class TestLog1 {

	static int sz_x=21;
	static int sz_y=41;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ImageJ();
		 
		float[] kx={0,1,2,1,0};
		float[] ky={0,2,4,6,-2,-4,-2,6,4,2,0};
		float[][] kernel={kx, ky};
		
		FloatProcessor fp_xy=new FloatProcessor(kx.length,kx.length);

		float[][] disp= new float[2][];

		disp[0]=GScaleSpace.joinXY(kernel, 0, 0);

		for (int i=0; i<kx.length*kx.length; i++)
			fp_xy.setf(i, disp[0][i]);

		new ImagePlus("kernel sep XY",fp_xy).show();
		printvector(disp[0],kx.length);
		 
		FloatProcessor fp_xz=new FloatProcessor(kx.length,ky.length);
	 
	 
		int sz=kx.length*ky.length;
		System.out.println("\n sz " +sz);
		disp[1]=GScaleSpace.joinXY(kernel, 0, 1);
  
		printvector(disp[1],kx.length);
		
		for (int i=0; i<sz; i++)
			fp_xz.setf(i, disp[1][i]);

		new ImagePlus("kernel sep XZ",fp_xz).show();
	}
	
	static void printvector(float[] data, int div) {
		for (int i=0; i<data.length; i++) {	
			if (i% div==0)
				System.out.print("\n");
			System.out.print(data[i]+",");		
		}

	}

}
