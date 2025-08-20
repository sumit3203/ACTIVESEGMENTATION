/**
 * 
 */
package test;

import static org.junit.Assert.*;
import ijaux.Util;
//import ijaux.datatype.ComplexArray;
//import ijaux.datatype.IComplexArray;
import ijaux.datatype.Pair;
import dsp.*;
import static dsp.DSP.*;
import static dsp.TestUtil.*;

import org.junit.Test;


/**
 * @author adminprodanov
 *
 */
public class FFTProcTest {

	static final float[] x={1,	2,	3,	9,	8,	5,	1,	2}; // 8 numbers
	static final float[] x2={1,0, 2,0, 3,0,	9,0, 8,0, 5,0, 1,0, 2,0};
	static final float[] xr={31,	-14.0710678118655f,	5,	0.0710678118654755f,	
			-5,	0.0710678118654755f,	5,	-14.0710678118655f	};
	
	static final float[] xi={0,	-4.82842712474619f,	4,	-0.828427124746190f,	
		0,	0.828427124746190f,	-4,	4.82842712474619f};
	
	/**
	 * Test method for {@link dsp.FFTProc#fftC2C1d(float[], float[], int, int)}.
	 */
	@Test
	public final void testFftC2C1d() {			
		float[] row1=x.clone();		
		System.out.println ("********************************");
		System.out.println ("FFT: fftC2C1d");
		float[] imag= new float[row1.length];
		
		int nfft=DSP.nfft(row1.length);		
		Pair<float[], float[]> carr= FFTProc.fftC2C1d(row1, imag, -1, nfft);
		float[] re=carr.first;
		float[] im=carr.second;
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\nReal part");
			Util.printFloatArray(re);
			System.out.println ("\nImaginary part");
			Util.printFloatArray(im);
		}
		assertEquals(true, (r1==1  &&  r2==1));
	}

	/**
	 * Test method for {@link dsp.FFTProc#fftR2C1d(float[], int, int)}.
	 */
	@Test
	public final void testFftR2C1d() {
		float[] row1=x.clone();	
		System.out.println ("********************************");
		System.out.println ("FFT: fftR2C1d");
		
		int nfft=nfft(row1.length);		
		Pair<float[], float[]> carr= FFTProc.fftR2C1d(row1, -1, nfft);
		float[] re=carr.first;
		float[] im=carr.second;
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\nReal part");
			Util.printFloatArray(re);
			System.out.println ("\nImaginary part");
			Util.printFloatArray(im);
		}
	
		assertEquals(true, (r1==1  &&  r2==1));
	}

	/**
	 * Test method for {@link dsp.FFTProc#cfft(float[], int, boolean)}.
	 */
	@Test
	public final void testCfft() {
		float[] row1=new float[x.length*2];
		
		for (int i=0, c=0; i< x.length; i++, c+=2) {
			row1[c]=x[i];
		}
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		System.out.println ("********************************");
		System.out.println ("FFT: cfft");
		
		int nfft=nfft(row1.length);		
		System.out.println ("nfft " +nfft +" len " + row1.length);
		FFTProc.cfft(row1,   true);
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		float[] re=new float[x.length];
		float[] im=new float[x.length];
		for (int i=0; i<re.length;i++) {
			re[i]=row1[2*i];
			im[i]=row1[2*i+1];
		}
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\n comp Real part");
			Util.printFloatArray(re);
			System.out.println ("\n exp Real part");
			Util.printFloatArray(xr);
			
			System.out.println ("\n comp Imaginary part");
			Util.printFloatArray(im);
			System.out.println ("\n exp Imaginary part");
			Util.printFloatArray(xi);
			
		}
	
		assertEquals(true, (r1==1  &&  r2==1));
	}
	
	
	/**
	 * Test method for {@link dsp.FFTProc#cfftp(float[], int, boolean)}.
	 */
	@Test
	public final void testCfftp() {
		float[] row1=new float[x.length*2];
		
		for (int i=0, c=0; i< x.length; i++, c+=2) {
			row1[c]=x[i];
		}
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		System.out.println ("********************************");
		System.out.println ("FFT: cfftp");
		
		int nfft=nfft(row1.length);		
		System.out.println ("nfft " +nfft +" len " + row1.length);
		FFTProc.cfftp(row1,  true, null);
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		float[] re=new float[x.length];
		float[] im=new float[x.length];
		for (int i=0; i<re.length;i++) {
			re[i]=row1[2*i];
			im[i]=row1[2*i+1];
		}
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\n comp Real part");
			Util.printFloatArray(re);
			System.out.println ("\n exp Real part");
			Util.printFloatArray(xr);
			
			System.out.println ("\n comp Imaginary part");
			Util.printFloatArray(im);
			System.out.println ("\n exp Imaginary part");
			Util.printFloatArray(xi);
			
		}
	
		assertEquals(true, (r1==1  &&  r2==1));
	}
	
	/**
	 * Test method for {@link dsp.FFTProc#rfft(float[], int, boolean)}.
	 */
	@Test
	public final void testRfft() {
		float[] row1=new float[x.length*2];
		
		for (int i=0, c=0; i< x.length; i++, c+=2) {
			row1[c]=x[i];
		}
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		System.out.println ("********************************");
		System.out.println ("FFT: cfft");
		
		int nfft=nfft(row1.length);		
		System.out.println ("nfft " +nfft +" len " + row1.length);
		FFTProc.rfft(row1);
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		float[] re=new float[x.length];
		float[] im=new float[x.length];
		for (int i=0; i<re.length;i++) {
			re[i]=row1[2*i];
			im[i]=row1[2*i+1];
		}
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\n comp Real part");
			Util.printFloatArray(re);
			System.out.println ("\n exp Real part");
			Util.printFloatArray(xr);
			
			System.out.println ("\n comp Imaginary part");
			Util.printFloatArray(im);
			System.out.println ("\n exp Imaginary part");
			Util.printFloatArray(xi);
			
		}
	
		assertEquals(true, (r1==1  &&  r2==1));
	}
	
	
	/**
	 * Test method for {@link dsp.FFTProc#rfftp(float[], int, boolean)}.
	 */
	@Test
	public final void testRfftp() {
		float[] row1=new float[x.length*2];
		
		for (int i=0, c=0; i< x.length; i++, c+=2) {
			row1[c]=x[i];
		}
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		System.out.println ("********************************");
		System.out.println ("FFT: cfft");
		
		int nfft=nfft(row1.length);		
		System.out.println ("nfft " +nfft +" len " + row1.length);
		FFTProc.rfft(row1);
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		float[] re=new float[x.length];
		float[] im=new float[x.length];
		for (int i=0; i<re.length;i++) {
			re[i]=row1[2*i];
			im[i]=row1[2*i+1];
		}
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\n comp Real part");
			Util.printFloatArray(re);
			System.out.println ("\n exp Real part");
			Util.printFloatArray(xr);
			
			System.out.println ("\n comp Imaginary part");
			Util.printFloatArray(im);
			System.out.println ("\n exp Imaginary part");
			Util.printFloatArray(xi);
			
		}
	
		assertEquals(true, (r1==1  &&  r2==1));
	}
	 
	
	/**
	 * Test method for {@link dsp.FFTProc#rfftp(float[], int, boolean)}.
	 */
	@Test
	public final void testRfftp2() {
		float[] row1=new float[x.length*2];
		
		for (int i=0, c=0; i< x.length; i++, c+=2) {
			row1[c]=x[i];
		}
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		System.out.println ("********************************");
		System.out.println ("FFT: cfft");
		
		int nfft=nfft(row1.length);		
		System.out.println ("nfft " +nfft +" len " + row1.length);
		Pair<double[], double[]> ptab=FFTUtil.expTable2 (row1.length, -1);
		FFTProc.rfftp(row1, ptab);
		
		//System.out.println ("\n row1");
		//Util.printFloatArray(row1);
		
		float[] re=new float[x.length];
		float[] im=new float[x.length];
		for (int i=0; i<re.length;i++) {
			re[i]=row1[2*i];
			im[i]=row1[2*i+1];
		}
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\n comp Real part");
			Util.printFloatArray(re);
			System.out.println ("\n exp Real part");
			Util.printFloatArray(xr);
			
			System.out.println ("\n comp Imaginary part");
			Util.printFloatArray(im);
			System.out.println ("\n exp Imaginary part");
			Util.printFloatArray(xi);
			
		}
	
		assertEquals(true, (r1==1  &&  r2==1));
	}
	 
 
	/**
	 * Test method for {@link dsp.FFTProc#fftR2Cp1d(float[], int, int)}.
	 */
	@Test
	public final void testFftR2Cp1d() {
		float[] row1=x.clone();	
		System.out.println ("********************************");
		System.out.println ("FFT: fftR2Cp1d");
		
		int nfft=nfft(row1.length);		
		Pair<float[], float[]> carr= FFTProc.fftR2Cp1d(row1, +1, nfft);
		float[] re=carr.first;
		float[] im=carr.second;
		
		double r1=corrcoef(re, xr);		 
		double r2=corrcoef(im, xi);
		
		boolean pass1=(r1==1);
		boolean pass2=(r2==1);
		System.out.println ("corr coeff real " +r1 + " test passed: " +pass1);
	
		System.out.println ("corr coeff imag " +r2 + " test passed: " + pass2);
		
		if (!pass1 || !pass2) {
			System.out.println ("\nReal part");
			Util.printFloatArray(re);
			System.out.println ("\nImaginary part");
			Util.printFloatArray(im);
		}
		
		//.ransformRadix2(x.clone(), new float[row1.length]);
		assertEquals(true, (pass1  &&  pass2));
	}

}
