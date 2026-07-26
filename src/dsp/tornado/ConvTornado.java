package dsp.tornado;

import dsp.IConv;
import ij.IJ;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.IJLineIteratorIP;
import ijaux.scale.IJLineIteratorStack;

import java.awt.Rectangle;
import java.util.*;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.exceptions.TornadoExecutionPlanException;
import uk.ac.manchester.tornado.api.types.arrays.FloatArray;


/**
 * @version 	1.2 23 Aug 2016
 *              1.1	14 Oct 2013
 * 				- moved contratAdjust -> Conv
 * 				- changed brightness adjustment factor to sigma^2		
 * 				1.1 	18 Jul 2013
 * 				- refactoring
 * 				1.0		05 Feb 2013 
 * 				Based on Mexican_Hat_Filter v 2.2
 * 				- common functionality is refactored in a library class
 * 				
 *   
 * 
 * @author Dimiter Prodanov IMEC , Sumit Kumar Vohra
 *
 *
 * @contents
 * This is Convolution Library that do different type of convolution
 * 
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

public class ConvTornado implements IConv {
	
	public static boolean debug=false;
	
	//CH
	private static final class Conv2DPlan {
	    TornadoExecutionPlan plan;
	    FloatArray in, k, out;
	    int kw, kh;
	}
	private static final HashMap<String, Conv2DPlan> conv2DCache = new HashMap<>();
	
	// CH - ST gradient block: 2 outputs (gradx, grady) from ONE source
	private static final class StGradPlan {
	    TornadoExecutionPlan plan;
	    FloatArray in, kx, kd;
	    FloatArray tGx, oGx, tGy, oGy;
	}
	private static final HashMap<String, StGradPlan> stGradCache = new HashMap<>();

	// CH - ST smoothing block: 3 outputs (gx2, gy2, gxy) from THREE sources
	private static final class StSmoothPlan {
	    TornadoExecutionPlan plan;
	    FloatArray inA, inB, inC, kx, kd;
	    FloatArray tA, oA, tB, oB, tC, oC;
	}
	private static final HashMap<String, StSmoothPlan> stSmoothCache = new HashMap<>();
	
	
	// CH - cache for convolveSep plans (separate from sep3Cache)
	private static final class SepPlan {
	    TornadoExecutionPlan plan;
	    FloatArray in, kx, kd;
	    FloatArray tmp, out;
	}
	private static final HashMap<String, SepPlan> sepCache = new HashMap<>();
	
	
	//CH - adding a cache class to store taskgraph object instead of creating one foe every run
	private static final class Sep3Plan {
	    TornadoExecutionPlan plan;
	    FloatArray in, kx, kd1, kd2;
	    FloatArray tGx,tGy,tXx,tYy,tXy;
	    FloatArray oGx,oGy,oXx,oYy,oXy;
	}
	private static final HashMap<String, Sep3Plan> sep3Cache = new HashMap<>();
	
	//CH - cache for convolveSemiSep
	private static final class SemiSepPlan {
	    TornadoExecutionPlan plan;
	    FloatArray in, kx, kd;
	    FloatArray tA, A, tB, B, out;
	}
	private static final HashMap<String, SemiSepPlan> semiSepCache = new HashMap<>();
	
	// CH - element-wise add: out = a + b
	public static void addArr(FloatArray a, FloatArray b, FloatArray out, int n) {
	    for (@Parallel int i = 0; i < n; i++) {
	        out.set(i, a.get(i) + b.get(i));
	    }
	}
	
	// 2D non-separable convolution, clamp-to-edge, row-major kernel (v outer, u inner)
	public static void conv2D(FloatArray in, FloatArray out, FloatArray k,
	                          int w, int h, int kw, int kh) {
	    final int uc = kw / 2, vc = kh / 2;
	    for (@Parallel int y = 0; y < h; y++) {
	        for (@Parallel int x = 0; x < w; x++) {
	            float sum = 0f;
	            int idx = 0;
	            for (int v = -vc; v <= vc; v++) {
	                int yy = y + v;
	                if (yy < 0) yy = 0;
	                if (yy >= h) yy = h - 1;
	                for (int u = -uc; u <= uc; u++) {
	                    int xx = x + u;
	                    if (xx < 0) xx = 0;
	                    if (xx >= w) xx = w - 1;
	                    sum += in.get(yy * w + xx) * k.get(idx);
	                    idx++;
	                }
	            }
	            out.set(y * w + x, sum);
	        }
	    }
	}
	
	// CH - X-direction: convolve along rows
	public static void convX(FloatArray in, FloatArray out, FloatArray k, int w, int h, int kw) {
	    for (@Parallel int y = 0; y < h; y++) {
	        for (@Parallel int x = 0; x < w; x++) {
	            float sum = 0f;
	            int L = k.getSize();
	            for (int c = 0; c < L; c++) {
	                int idx = x - c + kw;
	                if (idx < 0) idx = 0;
	                if (idx >= w) idx = w - 1;
	                sum += in.get(y * w + idx) * k.get(c);
	            }
	            out.set(y * w + x, sum);
	        }
	    }
	}

	// CH - Y-direction: convolve along columns
	public static void convY(FloatArray in, FloatArray out, FloatArray k, int w, int h, int kw) {
	    for (@Parallel int y = 0; y < h; y++) {
	        for (@Parallel int x = 0; x < w; x++) {
	            float sum = 0f;
	            int L = k.getSize();
	            for (int c = 0; c < L; c++) {
	                int idy = y - c + kw;
	                if (idy < 0) idy = 0;
	                if (idy >= h) idy = h - 1;
	                sum += in.get(idy * w + x) * k.get(c);
	            }
	            out.set(y * w + x, sum);
	        }
	    }
	}
	
	// parity check for 2D convolution of convolveFloat
	public static void parityConv2D(FloatProcessor src, float[] kernel, int kw, int kh) {
	    ConvTornado c = new ConvTornado();

	    // CPU reference = original 2D convolution
	    FloatProcessor cpu = (FloatProcessor) src.duplicate();
	    c.convolveFloatCPU(cpu, kernel, kw, kh);

	    // GPU candidate
	    FloatProcessor gpu = (FloatProcessor) src.duplicate();
	    c.convolveFloat(gpu, kernel, kw, kh);

	    float[] a=(float[])cpu.getPixels(), b=(float[])gpu.getPixels();
	    double max=0,sum=0,meanVal=0; int argmax=-1, w=src.getWidth();
	    for (int i=0;i<a.length;i++){
	        double d=Math.abs(a[i]-b[i]);
	        if(d>max){max=d;argmax=i;}
	        sum+=d; meanVal+=Math.abs(a[i]);
	    }
	    System.out.println(String.format(
	        "parityConv2D maxDiff=%.6g meanDiff=%.6g meanVal=%.4g worst@(%d,%d)",
	        max, sum/a.length, meanVal/a.length, argmax%w, argmax/w));
	}
	
	// parity check caller for convolveSemiSep
	public static void parityCheckSemiSep(FloatProcessor src, float[] kernx, float[] kern_diff) {
	    // test 1: full image
	    runSemiSepParity(src, kernx, kern_diff, null, "full");
	    // test 2: a sub-ROI (offset rectangle) — exercises ROI masking
	    java.awt.Rectangle roi = new java.awt.Rectangle(
	        src.getWidth()/4, src.getHeight()/4, src.getWidth()/2, src.getHeight()/2);
	    runSemiSepParity(src, kernx, kern_diff, roi, "roi");
	}

	// parity check for convolveSemiSep, on the basis of requirement - full image or a specific ROI 
	private static void runSemiSepParity(FloatProcessor src, float[] kernx, float[] kern_diff,
	                                     java.awt.Rectangle roi, String label) {
	    ConvTornado c = new ConvTornado();

	    FloatProcessor cpu = (FloatProcessor) src.duplicate();
	    if (roi != null) cpu.setRoi(roi);
	    {
	        FloatProcessor ip2 = (FloatProcessor) cpu.duplicate();
	        FloatProcessor ipx = (FloatProcessor) cpu.duplicate();
	        if (roi != null) { ip2.setRoi(roi); ipx.setRoi(roi); }
	        c.convolveFloat1D(ipx, kern_diff, kern_diff.length, 1);
	        c.convolveFloat1D(ipx, kernx, 1, kernx.length);
	        c.convolveFloat1D(ip2, kernx, kernx.length, 1);
	        c.convolveFloat1D(ip2, kern_diff, 1, kern_diff.length);
	        c.add(ip2, ipx, ip2.getRoi());
	        cpu.setPixels(ip2.getPixels());
	    }

	    FloatProcessor gpu = (FloatProcessor) src.duplicate();
	    if (roi != null) gpu.setRoi(roi);
	    c.convolveSemiSep(gpu, kernx, kern_diff);

	    float[] a=(float[])cpu.getPixels(), b=(float[])gpu.getPixels();
	    double max=0,sum=0,meanVal=0; int argmax=-1, w=src.getWidth();
	    for (int i=0;i<a.length;i++){
	        double d=Math.abs(a[i]-b[i]);
	        if(d>max){max=d;argmax=i;}
	        sum+=d; meanVal+=Math.abs(a[i]);
	    }
	    System.out.println(String.format(
	        "paritySemiSep[%s] maxDiff=%.6g meanDiff=%.6g meanVal=%.4g worst@(%d,%d)",
	        label, max, sum/a.length, meanVal/a.length, argmax%w, argmax/w));
	}
	
	// parity check for convolveSep
	public static void parityCheckSep(FloatProcessor src, float[] kernx, float[] kern_diff) {
	    ConvTornado c = new ConvTornado();

	    FloatProcessor cpu = (FloatProcessor) src.duplicate();
	    c.convolveFloat1D(cpu, kern_diff, kern_diff.length, 1);
	    c.convolveFloat1D(cpu, kernx, 1, kernx.length);

	    FloatProcessor gpu = (FloatProcessor) src.duplicate();
	    c.convolveSep(gpu, kernx, kern_diff);

	    float[] a=(float[])cpu.getPixels(), b=(float[])gpu.getPixels();
	    double max=0,sum=0,meanVal=0; int argmax=-1, w=src.getWidth();
	    for (int i=0;i<a.length;i++){
	        double d=Math.abs(a[i]-b[i]);
	        if(d>max){max=d;argmax=i;}
	        sum+=d; meanVal+=Math.abs(a[i]);
	    }
	    System.out.println(String.format(
	        "paritySep maxDiff=%.6g meanDiff=%.6g meanVal=%.4g worst@(%d,%d)",
	        max, sum/a.length, meanVal/a.length, argmax%w, argmax/w));
	}
	
	// parity check for convolveSep3
	public static void parityCheck(FloatProcessor src, float[] kernx, float[] kern_diff1, float[] kern_diff2) {
	    String[] names = {"gradx", "grady", "lap_xx", "lap_yy", "lap_xy"};
	    ConvTornado c = new ConvTornado();

	    // CPU reference (same 10 calls as the fallback)
	    FloatProcessor cGradx=(FloatProcessor)src.duplicate(), cGrady=(FloatProcessor)src.duplicate(),
	                   cLapXx=(FloatProcessor)src.duplicate(), cLapYy=(FloatProcessor)src.duplicate(),
	                   cLapXy=(FloatProcessor)src.duplicate();
	    c.convolveFloat1D(cGradx, kern_diff1, Ox);  c.convolveFloat1D(cGradx, kernx,      Oy);
	    c.convolveFloat1D(cGrady, kern_diff1, Oy);  c.convolveFloat1D(cGrady, kernx,      Ox);
	    c.convolveFloat1D(cLapXx, kern_diff2, Ox);  c.convolveFloat1D(cLapXx, kernx,      Oy);
	    c.convolveFloat1D(cLapYy, kern_diff2, Oy);  c.convolveFloat1D(cLapYy, kernx,      Ox);
	    c.convolveFloat1D(cLapXy, kern_diff1, Oy);  c.convolveFloat1D(cLapXy, kern_diff1, Ox);

	    // GPU result
	    FloatProcessor gGradx=(FloatProcessor)src.duplicate(), gGrady=(FloatProcessor)src.duplicate(),
	                   gLapXx=(FloatProcessor)src.duplicate(), gLapYy=(FloatProcessor)src.duplicate(),
	                   gLapXy=(FloatProcessor)src.duplicate();
	    c.convolveSep3(src, kernx, kern_diff1, kern_diff2, gGradx, gGrady, gLapXx, gLapYy, gLapXy);

	    FloatProcessor[] cpu = {cGradx, cGrady, cLapXx, cLapYy, cLapXy};
	    FloatProcessor[] gpu = {gGradx, gGrady, gLapXx, gLapYy, gLapXy};
	    int w = src.getWidth();

	    for (int o = 0; o < 5; o++) {
	        float[] a = (float[]) cpu[o].getPixels();
	        float[] b = (float[]) gpu[o].getPixels();
	        double max=0, sum=0, meanVal=0; int argmax=-1;
	        for (int i = 0; i < a.length; i++) {
	            double d = Math.abs(a[i] - b[i]);
	            if (d > max) { max = d; argmax = i; }
	            sum += d;
	            meanVal += Math.abs(a[i]);
	        }
	        System.out.println(String.format(
	            "parity %-7s maxDiff=%.6g meanDiff=%.6g meanVal=%.4g worst@(%d,%d)",
	            names[o], max, sum/a.length, meanVal/a.length,
	            argmax%w, argmax/w));
	    }
	}
	
	// CH - parity check for structure tensor
	public static void parityStructFull(FloatProcessor src, float[] kernx, float[] kern_diff1) {
	    ConvTornado c = new ConvTornado();
	    int w = src.getWidth(), n = src.getWidth() * src.getHeight();

	    // ---- Block 1 reference vs convolveStructGrad; Block 1 can be used as parity check for Gradient as well 
	    FloatProcessor oGx=(FloatProcessor)src.duplicate(), oGy=(FloatProcessor)src.duplicate();
	    c.convolveFloat1D(oGx, kern_diff1, Ox); c.convolveFloat1D(oGx, kernx, Oy);
	    c.convolveFloat1D(oGy, kern_diff1, Oy); c.convolveFloat1D(oGy, kernx, Ox);

	    FloatProcessor nGx=(FloatProcessor)src.duplicate(), nGy=(FloatProcessor)src.duplicate();
	    c.convolveStructGrad(src, kernx, kern_diff1, nGx, nGy);

	    diffPrint("stGrad gradx", (float[])oGx.getPixels(), (float[])nGx.getPixels(), w);
	    diffPrint("stGrad grady", (float[])oGy.getPixels(), (float[])nGy.getPixels(), w);

	    // ---- Block 2 reference vs convolveStructSmooth ----
//	     use gradients as realistic sources
	    FloatProcessor a1=(FloatProcessor)nGx.duplicate(), b1=(FloatProcessor)nGy.duplicate(), c1=(FloatProcessor)nGx.duplicate();
	    FloatProcessor a2=(FloatProcessor)nGx.duplicate(), b2=(FloatProcessor)nGy.duplicate(), c2=(FloatProcessor)nGx.duplicate();

	    c.convolveFloat1D(a1, kern_diff1, Ox); c.convolveFloat1D(a1, kernx, Oy);
	    c.convolveFloat1D(b1, kern_diff1, Oy); c.convolveFloat1D(b1, kernx, Ox);
	    c.convolveFloat1D(c1, kern_diff1, Oy); c.convolveFloat1D(c1, kernx, Ox);

	    c.convolveStructSmooth(kernx, kern_diff1, a2, b2, c2);

	    diffPrint("stSmooth gx2", (float[])a1.getPixels(), (float[])a2.getPixels(), w);
	    diffPrint("stSmooth gy2", (float[])b1.getPixels(), (float[])b2.getPixels(), w);
	    diffPrint("stSmooth gxy", (float[])c1.getPixels(), (float[])c2.getPixels(), w);
	}

	private static void diffPrint(String name, float[] a, float[] b, int w) {
	    double max=0, sum=0, meanVal=0; int argmax=-1;
	    for (int i=0;i<a.length;i++){ double d=Math.abs(a[i]-b[i]); if(d>max){max=d;argmax=i;} sum+=d; meanVal+=Math.abs(a[i]); }
	    System.out.println(String.format("parity %-14s maxDiff=%.6g meanDiff=%.6g meanVal=%.4g worst@(%d,%d)",
	        name, max, sum/a.length, meanVal/a.length, argmax%w, argmax/w));
	}
	

	/**
	 * Structure Tensor gradient block: from one source, produce
	 *   gradx = X(kern_diff1) -> Y(kernx)
	 *   grady = Y(kern_diff1) -> X(kernx)
	 * Single fused graph on validated convX/convY. In-place into caller's processors.
	 */
	@Override
	public void convolveStructGrad(FloatProcessor src, float[] kernx, float[] kern_diff1,
	                               FloatProcessor gradx, FloatProcessor grady) {
	    final int w = src.getWidth(), h = src.getHeight(), n = w * h;
	    try {
	        String key = "stgrad:" + w + "x" + h + ":" + kernx.length + ":" + kern_diff1.length;
	        StGradPlan p = stGradCache.get(key);
	        boolean built=p==null;
	        
	        if (p == null) {
	            p = new StGradPlan();
	            p.in  = new FloatArray(n);
	            p.kx  = FloatArray.fromArray(kernx);
	            p.kd  = FloatArray.fromArray(kern_diff1);
	            p.tGx = new FloatArray(n); p.oGx = new FloatArray(n);
	            p.tGy = new FloatArray(n); p.oGy = new FloatArray(n);

	            final int kwx = kernx.length / 2, kwd = kern_diff1.length / 2;

	            TaskGraph tg = new TaskGraph(key)
	                .transferToDevice(DataTransferMode.FIRST_EXECUTION, p.kx, p.kd)
	                .transferToDevice(DataTransferMode.EVERY_EXECUTION, p.in)
	                // gradx: X(kern_diff1) -> Y(kernx)
	                .task("gx1", ConvTornado::convX, p.in,  p.tGx, p.kd, w, h, kwd)
	                .task("gx2", ConvTornado::convY, p.tGx, p.oGx, p.kx, w, h, kwx)
	                // grady: Y(kern_diff1) -> X(kernx)
	                .task("gy1", ConvTornado::convY, p.in,  p.tGy, p.kd, w, h, kwd)
	                .task("gy2", ConvTornado::convX, p.tGy, p.oGy, p.kx, w, h, kwx)
	                .transferToHost(DataTransferMode.EVERY_EXECUTION, p.oGx, p.oGy);

	            p.plan = new TornadoExecutionPlan(tg.snapshot());
	            stGradCache.put(key, p);
	        }

	     // fill
	        float[] s = (float[]) src.getPixels();
	        long tFill = System.nanoTime();
	        for (int i = 0; i < n; i++) p.in.set(i, s[i]);
	        long fillNs = System.nanoTime() - tFill;

	        // execute
	        long t1 = System.nanoTime();
	        p.plan.execute();
	        long execNs = System.nanoTime() - t1;

	        // copy back
	        long t2 = System.nanoTime();
	        copyBack(p.oGx, gradx);
	        copyBack(p.oGy, grady);
	        long copyNs = System.nanoTime() - t2;

//	        System.out.println(String.format("stgrad %s built=%b fill=%.1fms exec=%.1fms copy=%.1fms TOTAL=%.1fms",
//	                key, (built), fillNs/1e6, execNs/1e6, copyNs/1e6, (fillNs+execNs+copyNs)/1e6));

//	        System.out.println(String.format("built = "+built+" stGrad %s TOTAL=%.1fms", key, (fillNs + execNs + copyNs)/1e6));
	        
	    } catch (Throwable t) {
	        System.out.println("convolveStructGrad GPU failed, CPU fallback: " + t.getMessage());
	        // exact original calls
	        System.arraycopy(src.getPixels(), 0, gradx.getPixels(), 0, n);
	        System.arraycopy(src.getPixels(), 0, grady.getPixels(), 0, n);
	        convolveFloat1D(gradx, kern_diff1, Ox); convolveFloat1D(gradx, kernx, Oy);
	        convolveFloat1D(grady, kern_diff1, Oy); convolveFloat1D(grady, kernx, Ox);
	    }
	}
	
	/**
	 * Structure Tensor smoothing block: three sources, each smoothed
	 *   gx2 = X(kern_diff1) -> Y(kernx)
	 *   gy2 = Y(kern_diff1) -> X(kernx)
	 *   gxy = Y(kern_diff1) -> X(kernx)
	 * Single fused graph. In-place: each result written back into its own processor.
	 */
	@Override
	public void convolveStructSmooth(float[] kernx, float[] kern_diff1,
	                                 FloatProcessor gx2, FloatProcessor gy2, FloatProcessor gxy) {
	    final int w = gx2.getWidth(), h = gx2.getHeight(), n = w * h;
	    try {
	        String key = "stsmooth:" + w + "x" + h + ":" + kernx.length + ":" + kern_diff1.length;
	        StSmoothPlan p = stSmoothCache.get(key);
	        boolean built=p==null;
	        
	        if (p == null) {
	            p = new StSmoothPlan();
	            p.inA = new FloatArray(n); p.inB = new FloatArray(n); p.inC = new FloatArray(n);
	            p.kx  = FloatArray.fromArray(kernx);
	            p.kd  = FloatArray.fromArray(kern_diff1);
	            p.tA = new FloatArray(n); p.oA = new FloatArray(n);
	            p.tB = new FloatArray(n); p.oB = new FloatArray(n);
	            p.tC = new FloatArray(n); p.oC = new FloatArray(n);

	            final int kwx = kernx.length / 2, kwd = kern_diff1.length / 2;

	            TaskGraph tg = new TaskGraph(key)
	                .transferToDevice(DataTransferMode.FIRST_EXECUTION, p.kx, p.kd)
	                .transferToDevice(DataTransferMode.EVERY_EXECUTION, p.inA, p.inB, p.inC)
	                // gx2: X(kern_diff1) -> Y(kernx)
	                .task("a1", ConvTornado::convX, p.inA, p.tA, p.kd, w, h, kwd)
	                .task("a2", ConvTornado::convY, p.tA,  p.oA, p.kx, w, h, kwx)
	                // gy2: Y(kern_diff1) -> X(kernx)
	                .task("b1", ConvTornado::convY, p.inB, p.tB, p.kd, w, h, kwd)
	                .task("b2", ConvTornado::convX, p.tB,  p.oB, p.kx, w, h, kwx)
	                // gxy: Y(kern_diff1) -> X(kernx)
	                .task("c1", ConvTornado::convY, p.inC, p.tC, p.kd, w, h, kwd)
	                .task("c2", ConvTornado::convX, p.tC,  p.oC, p.kx, w, h, kwx)
	                .transferToHost(DataTransferMode.EVERY_EXECUTION, p.oA, p.oB, p.oC);

	            p.plan = new TornadoExecutionPlan(tg.snapshot());
	            stSmoothCache.put(key, p);
	        }

	     // fill
	        float[] a = (float[]) gx2.getPixels();
	        float[] b = (float[]) gy2.getPixels();
	        float[] c = (float[]) gxy.getPixels();
	        long tFill = System.nanoTime();
	        for (int i = 0; i < n; i++) { p.inA.set(i, a[i]); p.inB.set(i, b[i]); p.inC.set(i, c[i]); }
	        long fillNs = System.nanoTime() - tFill;

	        // execute
	        long t1 = System.nanoTime();
	        p.plan.execute();
	        long execNs = System.nanoTime() - t1;

	        // copy back
	        long t2 = System.nanoTime();
	        copyBack(p.oA, gx2);
	        copyBack(p.oB, gy2);
	        copyBack(p.oC, gxy);
	        long copyNs = System.nanoTime() - t2;

//	        System.out.println(String.format("stsmooth %s built=%b fill=%.1fms exec=%.1fms copy=%.1fms TOTAL=%.1fms",
//	                key, (built), fillNs/1e6, execNs/1e6, copyNs/1e6, (fillNs+execNs+copyNs)/1e6));
//	        System.out.println(String.format("built = "+built+" stsmooth %s TOTAL=%.1fms", key, (fillNs + execNs + copyNs)/1e6));
	        
	    } catch (Throwable t) {
	        System.out.println("convolveStructSmooth GPU failed, CPU fallback: " + t.getMessage());
	        convolveFloat1D(gx2, kern_diff1, Ox); convolveFloat1D(gx2, kernx, Oy);
	        convolveFloat1D(gy2, kern_diff1, Oy); convolveFloat1D(gy2, kernx, Ox);
	        convolveFloat1D(gxy, kern_diff1, Oy); convolveFloat1D(gxy, kernx, Ox);
	    }
	}
	
	//CH - adding this to decrease the time utilization with multiple convolveFloat1D calls; applied in Hessian, Gaussian Curvature I, Curvature 2D, Curvature 3D, Anistropic Log, Wenington componenets
	@Override
	public void convolveSep3(FloatProcessor src, float[] kernx, float[] kern_diff1, float[] kern_diff2,
	        FloatProcessor gradx, FloatProcessor grady,
	        FloatProcessor lap_xx, FloatProcessor lap_yy, FloatProcessor lap_xy) {

	    final int w = src.getWidth(), h = src.getHeight(), n = w * h;

	    try {
	        String key = w + "x" + h + ":" + kernx.length + ":" + kern_diff1.length + ":" + kern_diff2.length;
	        long tBuild = 0, tExec, tCopy;
	        Sep3Plan p = sep3Cache.get(key);
	        boolean built = (p == null);

	        long t0 = System.nanoTime();
	        

	        if (p == null) {                      
	            p = new Sep3Plan();
	            p.in  = new FloatArray(n);
	            p.kx  = FloatArray.fromArray(kernx);
	            p.kd1 = FloatArray.fromArray(kern_diff1);
	            p.kd2 = FloatArray.fromArray(kern_diff2);
	            p.tGx=new FloatArray(n); p.tGy=new FloatArray(n); p.tXx=new FloatArray(n);
	            p.tYy=new FloatArray(n); p.tXy=new FloatArray(n);
	            p.oGx=new FloatArray(n); p.oGy=new FloatArray(n); p.oXx=new FloatArray(n);
	            p.oYy=new FloatArray(n); p.oXy=new FloatArray(n);

	            final int kwx=kernx.length/2, kw1=kern_diff1.length/2, kw2=kern_diff2.length/2;

	            TaskGraph tg = new TaskGraph("sep3_"+key)
	                // kernels uploaded once; input re-uploaded each run (EVERY_EXECUTION)
	                .transferToDevice(DataTransferMode.FIRST_EXECUTION, p.kx, p.kd1, p.kd2)
	                .transferToDevice(DataTransferMode.EVERY_EXECUTION, p.in)
	                .task("gx1", ConvTornado::convX, p.in,  p.tGx, p.kd1, w, h, kw1)
	                .task("gx2", ConvTornado::convY, p.tGx, p.oGx, p.kx,  w, h, kwx)
	                .task("gy1", ConvTornado::convY, p.in,  p.tGy, p.kd1, w, h, kw1)
	                .task("gy2", ConvTornado::convX, p.tGy, p.oGy, p.kx,  w, h, kwx)
	                .task("xx1", ConvTornado::convX, p.in,  p.tXx, p.kd2, w, h, kw2)
	                .task("xx2", ConvTornado::convY, p.tXx, p.oXx, p.kx,  w, h, kwx)
	                .task("yy1", ConvTornado::convY, p.in,  p.tYy, p.kd2, w, h, kw2)
	                .task("yy2", ConvTornado::convX, p.tYy, p.oYy, p.kx,  w, h, kwx)
	                .task("xy1", ConvTornado::convY, p.in,  p.tXy, p.kd1, w, h, kw1)
	                .task("xy2", ConvTornado::convX, p.tXy, p.oXy, p.kd1, w, h, kw1)
	                .transferToHost(DataTransferMode.EVERY_EXECUTION, p.oGx,p.oGy,p.oXx,p.oYy,p.oXy);

	            ImmutableTaskGraph itg = tg.snapshot();
	            p.plan = new TornadoExecutionPlan(itg);
	            sep3Cache.put(key, p);
	            tBuild = System.nanoTime() - t0;
	        }

	        // every call: refill input, re-execute compiled plan
	        float[] s = (float[]) src.getPixels();
	        long tFill = System.nanoTime();
	        for (int i = 0; i < n; i++) p.in.set(i, s[i]);
	        long fillMs = System.nanoTime() - tFill;
//	        System.out.println(String.format("sep3 %s fill=%.1fms", key, fillMs/1e6));


	        long t1 = System.nanoTime();
	        p.plan.execute();
	        tExec = System.nanoTime() - t1;
	       
	        long t2 = System.nanoTime();
	        copyBack(p.oGx, gradx);  copyBack(p.oGy, grady);
	        copyBack(p.oXx, lap_xx); copyBack(p.oYy, lap_yy); copyBack(p.oXy, lap_xy);
	        tCopy = System.nanoTime() - t2;

//	        System.out.println(String.format("sep3 %s built=%b build=%.1fms exec=%.1fms copy=%.1fms",
//	                key, built, tBuild/1e6, tExec/1e6, tCopy/1e6));
//	        System.out.println(String.format("built = "+built+" sep3 %s TOTAL=%.1fms", key, (fillMs + tExec + tCopy)/1e6));


	    } catch (Throwable t) {                    // any Tornado failure -> CPU fallback
	        IJ.log("convolveSep3 GPU failed, CPU fallback: " + t.getMessage());
	        convolveFloat1D(gradx,  kern_diff1, Ox);  convolveFloat1D(gradx,  kernx,      Oy);
	        convolveFloat1D(grady,  kern_diff1, Oy);  convolveFloat1D(grady,  kernx,      Ox);
	        convolveFloat1D(lap_xx, kern_diff2, Ox);  convolveFloat1D(lap_xx, kernx,      Oy);
	        convolveFloat1D(lap_yy, kern_diff2, Oy);  convolveFloat1D(lap_yy, kernx,      Ox);
	        convolveFloat1D(lap_xy, kern_diff1, Oy);  convolveFloat1D(lap_xy, kern_diff1, Ox);
	    }
	}

		    private static void copyBack(FloatArray a, FloatProcessor fp) {
		        float[] px = (float[]) fp.getPixels();
		        System.arraycopy(a.toHeapArray(), 0, px, 0, px.length);
		    }
	
	
	/**
	 * It is used for semi-separable convolution
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	@Override
	public void convolveSemiSep(FloatProcessor ip, float[] kernx, float[] kern_diff) {
	    final int w = ip.getWidth(), h = ip.getHeight(), n = w * h;

	    try {
	        String key = "semisep:" + w + "x" + h + ":" + kernx.length + ":" + kern_diff.length;
	        long tBuild = 0, tExec, tCopy;
	        SemiSepPlan p = semiSepCache.get(key);
	        boolean built = (p == null);

	        long t0 = System.nanoTime();

	        if (p == null) {
	            p = new SemiSepPlan();
	            p.in = new FloatArray(n);
	            p.kx = FloatArray.fromArray(kernx);
	            p.kd = FloatArray.fromArray(kern_diff);
	            p.tA = new FloatArray(n); p.A = new FloatArray(n);
	            p.tB = new FloatArray(n); p.B = new FloatArray(n);
	            p.out = new FloatArray(n);

	            final int kwx = kernx.length / 2;
	            final int kwd = kern_diff.length / 2;

	            TaskGraph tg = new TaskGraph(key)
	                .transferToDevice(DataTransferMode.FIRST_EXECUTION, p.kx, p.kd)
	                .transferToDevice(DataTransferMode.EVERY_EXECUTION, p.in)
	                // Path A: X(kern_diff) -> Y(kernx)
	                .task("ax", ConvTornado::convX, p.in, p.tA, p.kd, w, h, kwd)
	                .task("ay", ConvTornado::convY, p.tA, p.A,  p.kx, w, h, kwx)
	                // Path B: X(kernx) -> Y(kern_diff)
	                .task("bx", ConvTornado::convX, p.in, p.tB, p.kx, w, h, kwx)
	                .task("by", ConvTornado::convY, p.tB, p.B,  p.kd, w, h, kwd)
	                // out = A + B
	                .task("sum", ConvTornado::addArr, p.A, p.B, p.out, n)
	                .transferToHost(DataTransferMode.EVERY_EXECUTION, p.out);

	            ImmutableTaskGraph itg = tg.snapshot();
	            p.plan = new TornadoExecutionPlan(itg);
	            semiSepCache.put(key, p);
	            tBuild = System.nanoTime() - t0;
	        }

	        float[] s = (float[]) ip.getPixels();
	        long tFill = System.nanoTime();
	        for (int i = 0; i < n; i++) p.in.set(i, s[i]);
	        long fillMs = System.nanoTime() - tFill;
//	        System.out.println(String.format("semisep %s fill=%.1fms", key, fillMs/1e6));

	        long t1 = System.nanoTime();
	        p.plan.execute();
	        tExec = System.nanoTime() - t1;

	        long t2 = System.nanoTime();
	        float[] dst = (float[]) ip.getPixels();
	        System.arraycopy(p.out.toHeapArray(), 0, dst, 0, n);
	        tCopy = System.nanoTime() - t2;

//	        System.out.println(String.format("semisep %s built=%b build=%.1fms exec=%.1fms copy=%.1fms",
//	                key, built, tBuild/1e6, tExec/1e6, tCopy/1e6));
//	        System.out.println(String.format("built = "+built+" semisep %s TOTAL=%.1fms", key, (fillMs + tExec + tCopy)/1e6));


	    } catch (Throwable t) {
	        IJ.log("convolveSemiSep GPU failed, CPU fallback: " + t.getMessage());
	        // exact CPU fallback (original logic)
	        FloatProcessor ip2 = (FloatProcessor) ip.duplicate();
	        FloatProcessor ipx = (FloatProcessor) ip.duplicate();
	        convolveFloat1D(ipx, kern_diff, kern_diff.length, 1);
	        convolveFloat1D(ipx, kernx, 1, kernx.length);
	        convolveFloat1D(ip2, kernx, kernx.length, 1);
	        convolveFloat1D(ip2, kern_diff, 1, kern_diff.length);
	        add(ip2, ipx, ip2.getRoi());
	        ip.setPixels(ip2.getPixels());
	    }
	}
	
	
	/**
	 * It is used for semi-separable convolution with 
	 * iteration rather than synchronizion
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	@Override
	public void convolveSemiSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff) {
		FloatProcessor ip2 = null;
		FloatProcessor ipx = null;
		final Rectangle roi=ip.getRoi();
		
		ip2 = (FloatProcessor)ip.duplicate();
		ip2.setRoi(roi);
		ipx = (FloatProcessor)ip.duplicate();
		ipx.setRoi(roi);
		
		convolveFloat1D(ipx, kern_diff, Ox); // x direction
		//ipx.setSnapshotPixels(null);
		convolveFloat1D(ipx, kernx, Oy); // y direction
		//new ImagePlus("cx", ipx).show();
		
		convolveFloat1D(ip2, kernx, Ox); // x direction
		//ip2.setSnapshotPixels(null);
		convolveFloat1D(ip2, kern_diff, Oy); // y direction
		//new ImagePlus("cy", ip2).show();
		add(ip2, ipx, ip.getRoi());
		//new ImagePlus("roi", ip2).show();
		ip.setPixels(ip2.getPixels());
	}
	
	/**
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
	@Override
	public void convolveSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff) {
		convolveFloat1D(ip, kern_diff, Ox); // x direction
		//ipx.setSnapshotPixels(null);
		convolveFloat1D(ip, kernx, Oy); // y direction
		//new ImagePlus("cx", ipx).show();	
	}
	
	/**
	 * @param ip
	 * @param kernx
	 * @param kern_diff
	 */
//	@Override
//	public void convolveSep(ImageProcessor ip, float[] kernx, float[] kern_diff) {
//		convolveFloat1D(ip, kern_diff, kern_diff.length, 1); // x direction
//		//ipx.setSnapshotPixels(null);
//		convolveFloat1D(ip, kernx, 1, kernx.length); // y direction
//		//new ImagePlus("cx", ipx).show();	
//	}
	
	@Override
	public void convolveSep(ImageProcessor ip, float[] kernx, float[] kern_diff) {
	    final int w = ip.getWidth(), h = ip.getHeight(), n = w * h;

	    try {
	        String key = "sep:" + w + "x" + h + ":" + kernx.length + ":" + kern_diff.length;
	        long tBuild = 0, tExec, tCopy;
	        SepPlan p = sepCache.get(key);
	        boolean built = (p == null);
	        
	        long t0 = System.nanoTime();

	        if (p == null) {
	            p = new SepPlan();
	            p.in  = new FloatArray(n);
	            p.kx  = FloatArray.fromArray(kernx);
	            p.kd  = FloatArray.fromArray(kern_diff);
	            p.tmp = new FloatArray(n);   // after X-pass, stays on device
	            p.out = new FloatArray(n);   // after Y-pass, returned

	            final int kwd = kern_diff.length / 2;   // X uses kern_diff
	            final int kwx = kernx.length / 2;        // Y uses kernx

	            TaskGraph tg = new TaskGraph(key)
	                .transferToDevice(DataTransferMode.FIRST_EXECUTION, p.kx, p.kd)
	                .transferToDevice(DataTransferMode.EVERY_EXECUTION, p.in)
	                // X pass: kern_diff along rows  -> tmp
	                .task("sx", ConvTornado::convX, p.in,  p.tmp, p.kd, w, h, kwd)
	                // Y pass: kernx along columns   -> out
	                .task("sy", ConvTornado::convY, p.tmp, p.out, p.kx, w, h, kwx)
	                .transferToHost(DataTransferMode.EVERY_EXECUTION, p.out);

	            ImmutableTaskGraph itg = tg.snapshot();
	            p.plan = new TornadoExecutionPlan(itg);
	            sepCache.put(key, p);
	            tBuild = System.nanoTime() - t0;
	        }

	        // fill input from ip (convert to float view)
	        FloatProcessor fp = (ip instanceof FloatProcessor)
	                ? (FloatProcessor) ip
	                : (FloatProcessor) ip.convertToFloat();
	        float[] s = (float[]) fp.getPixels();
	        long tFill = System.nanoTime();
	        for (int i = 0; i < n; i++) p.in.set(i, s[i]);
	        long fillMs = System.nanoTime() - tFill;
//	        System.out.println(String.format("sep %s fill=%.1fms", key, fillMs/1e6));

	        long t1 = System.nanoTime();
	        p.plan.execute();
	        tExec = System.nanoTime() - t1;

	        long t2 = System.nanoTime();
	        float[] dst = (float[]) fp.getPixels();
	        System.arraycopy(p.out.toHeapArray(), 0, dst, 0, n);
	        if (ip != fp) ip.setPixels(fp.getPixels());
	        tCopy = System.nanoTime() - t2;

//	        System.out.println(String.format("sep %s built=%b build=%.1fms exec=%.1fms copy=%.1fms",
//	                key, built, tBuild/1e6, tExec/1e6, tCopy/1e6));

//	        System.out.println(String.format("built = "+built+" sep %s TOTAL=%.1fms", key, (fillMs + tExec + tCopy)/1e6));

	    } catch (Throwable t) {
	        IJ.log("convolveSep GPU failed, CPU fallback: " + t.getMessage());
	        convolveFloat1D(ip, kern_diff, kern_diff.length, 1);
	        convolveFloat1D(ip, kernx, 1, kernx.length);
	    }
	}
	
	/**
	 * @param ip
	 * @param kernx
	 * @param kernx
	 */
	@Override
	public void convolveSemiSep(ImageStack xstack, float[] kernx, float[] kerny, float[] kernz) {
		
		long time=-System.nanoTime();
		ImageStack ystack=cloneStack(xstack);
		ImageStack zstack=cloneStack(xstack);
		
		time+=System.nanoTime();	
		time/=1000.0f;
		//System.out.println("cloning time: " + time +" us");			
		time=-System.nanoTime();
	
		convolveFloat1D(xstack, kernx, Ox); // X
		convolveFloat1D(xstack, kerny, Oy); // Y
		convolveFloat1D(xstack, kernz, Oz); // Z
		
		convolveFloat1D(ystack, kernx, Oy); // Y
		convolveFloat1D(ystack, kerny, Ox); // X	
		convolveFloat1D(ystack, kernz, Oz); // Z
		
		convolveFloat1D(zstack, kernx, Oz); // Z		
		convolveFloat1D(zstack, kerny, Ox); // X
		convolveFloat1D(zstack, kernz, Oy); // Y

		
		addToStack(xstack,ystack, zstack);
		//new ImagePlus("xstack", xstack).show();
		ystack=null;
		zstack=null;
		
	
		time+=System.nanoTime();	
		time/=1000.0f;
		System.out.println("processing time: " + time +" us");
	}
	
	
	
	/**
	 * It is used to convolve 3D Stack
	 * @param xstack
	 * @param kernx
	 * @param kern_diffx
	 * @param kernz
	 */
	@Override
	public void convolveSep3D(ImageStack xstack,  float[] kernx,
			float[] kern_diffx, float[] kernz) {
		 convolveFloat1D(xstack, kern_diffx, Ox);
		 convolveFloat1D(xstack, kernx, Oy);
		 convolveFloat1D(xstack, kernz, Oz);
	}

	
	private void addToStack (ImageStack dest, ImageStack a, ImageStack b) {
		int bitdepth=dest.getBitDepth();
		
		if (bitdepth!=a.getBitDepth() || a.getBitDepth()!=b.getBitDepth())
			return;
		final int sz=dest.getSize();
		for (int i=1; i<=sz; i++) {

			 switch (bitdepth) {
				 case 8: {
					 byte[] pixels= (byte[])dest.getPixels(i);
					 byte[] pixels_a= (byte[])a.getPixels(i);
					 byte[] pixels_b=(byte[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
				 case 16: {
					 short[] pixels= (short[])dest.getPixels(i);
					 short[] pixels_a= (short[])a.getPixels(i);
					 short[] pixels_b=(short[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
				 case 24: {
					 int[] pixels= (int[])dest.getPixels(i);
					 int[] pixels_a= (int[])a.getPixels(i);
					 int[] pixels_b=(int[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
				 case 32: {
					 float[] pixels= (float[])dest.getPixels(i);
					 float[] pixels_a= (float[])a.getPixels(i);
					 float[] pixels_b=(float[])b.getPixels(i);
					 
					 for (int c=0; c<pixels.length; c++)
						 pixels[c]+=pixels_a[c]+pixels_b[c];
					 break;
				 }
			 }
			 
			 
		 }
	}
	
	public static ImageStack cloneStack(ImageStack is) {
		final int width=is.getWidth();
		final int height=is.getHeight();
		Object[] array=is.getImageArray();
				
		ImageStack ret=ImageStack.create(width, height, array.length, is.getBitDepth());
		
		Object[] array2 =array.clone();
		int cnt=1;
		for (Object o: array2)
			ret.setPixels(o, cnt++);
		
		ret.update(is.getProcessor(1));
		ret.setRoi(is.getRoi());
		
		
		return ret;
	}
	/**
	 * @param dest
	 * @param src
	 * @param r
	 */
	private void add(ImageProcessor dest, ImageProcessor src, Rectangle r) {
		for (int y=r.y; y<r.y+r.height; y++) {
			for (int x=r.x;x<r.x+r.width; x++) {
				float sum = dest.getf(x,y) + src.getf(x,y);
				dest.setf(x, y, sum);
			}
		}
	}
	
	@Override
	public boolean convolveFloat(ImageProcessor ip, float[] kernel, int kw, int kh) {
	    final int w = ip.getWidth(), h = ip.getHeight(), n = w * h;
	    final java.awt.Rectangle roi = ip.getRoi();

	    // ROI or masked -> CPU (GPU graph is full-image; same reasoning as semisep)
	    boolean fullImage = (ip.getMask() == null &&
	        roi.x == 0 && roi.y == 0 && roi.width == w && roi.height == h);
	    if (!fullImage) {
	        return convolveFloatCPU(ip, kernel, kw, kh);
	    }

	    try {
	        // key includes kw x kh (structure). Kernel VALUES uploaded every execution below.
	        String key = "conv2d:" + w + "x" + h + ":" + kw + "x" + kh;
	        Conv2DPlan p = conv2DCache.get(key);
	        boolean built = (p == null);

	        if (p == null) {
	            p = new Conv2DPlan();
	            p.in  = new FloatArray(n);
	            p.k   = new FloatArray(kernel.length);
	            p.out = new FloatArray(n);
	            p.kw = kw; p.kh = kh;
	            TaskGraph tg = new TaskGraph(key)
	                // kernel + input BOTH every execution (kernel VALUES differ per filter)
	                .transferToDevice(DataTransferMode.EVERY_EXECUTION, p.in, p.k)
	                .task("c2d", ConvTornado::conv2D, p.in, p.out, p.k, w, h, kw, kh)
	                .transferToHost(DataTransferMode.EVERY_EXECUTION, p.out);
	            p.plan = new TornadoExecutionPlan(tg.snapshot());
	            conv2DCache.put(key, p);
	        }

	        // fill input AND kernel (kernel refilled every call -> correct per-filter values)
	        float[] s = (float[]) ip.getPixelsCopy();
	        long tFill = System.nanoTime();
	        for (int i = 0; i < n; i++) p.in.set(i, s[i]);
	        for (int i = 0; i < kernel.length; i++) p.k.set(i, kernel[i]);
	        long fillNs = System.nanoTime() - tFill;

	        long t1 = System.nanoTime();
	        p.plan.execute();
	        long execNs = System.nanoTime() - t1;

	        long t2 = System.nanoTime();
	        float[] dst = (float[]) ip.getPixels();
	        System.arraycopy(p.out.toHeapArray(), 0, dst, 0, n);
	        long copyNs = System.nanoTime() - t2;
	        
//	        System.out.println(String.format("built = "+built+" conv2D %s TOTAL=%.1fms", key, (fillNs + execNs + copyNs)/1e6));
	        return true;

	    } catch (Throwable t) {
	        IJ.log("convolveFloat GPU failed, CPU fallback: " + t.getMessage());
	        return convolveFloatCPU(ip, kernel, kw, kh);
	    }
	}

	/** Convolves the float image <code>ip</code> with a kernel of width 
	<code>kw</code> and height <code>kh</code>. Returns false if 
	the user cancels the operation by pressing 'Esc'. */
	/**
	 * @param ip
	 * @param kernel
	 * @param kw
	 * @param kh
	 * @param scaled
	 * @return
	 */
	public boolean convolveFloatCPU(ImageProcessor ip, float[] kernel, int kw, int kh) {

		int width = ip.getWidth();
		int height = ip.getHeight();
		Rectangle r = ip.getRoi();
		boolean nonRectRoi = ip.getMask()!=null;
		if (nonRectRoi)
			ip.snapshot();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = x1 + r.width;
		int y2 = y1 + r.height;
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getPixelsCopy();
		 
		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		//long lastTime = System.currentTimeMillis();
		for(int y=y1; y<y2; y++) {
			 
			for(int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				edgePixel = y<vc || y>=yedge || x<uc || x>=xedge;
				for(int v=-vc; v <= vc; v++) {
					offset = x+(y+v)*width;
					for(int u = -uc; u <= uc; u++) {
						if (edgePixel) {
							if (i>=kernel.length) // work around for JIT compiler bug on Linux
								IJ.log("kernel index error: "+i);
							sum += getPixel(x+u, y+v, pixels2, width, height)*kernel[i++];
						} else
							sum += pixels2[offset+u]*kernel[i++];
					}
				}
				pixels[x+y*width] = (float)(sum);
			}
		}
		if (nonRectRoi)
			ip.reset(ip.getMask());
		return true;
	}
	
	
	@Override
	public void convolveFloat1D(FloatProcessor fp, float[] kernel, int xdir) {
		IJLineIteratorIP<float[]> iter= new IJLineIteratorIP<float[]>(fp, xdir);
		
		final int width=fp.getWidth();
		final int height=fp.getHeight();
		FloatProcessor ret=new FloatProcessor(width, height);
		
		int cnt=0;
		if (debug) {
			printvector(kernel);
			System.out.println();
		}
		while (iter.hasNext()) {
			//System.out.println(" c: "+cnt);
			final float[] line=iter.next();	
			final float[] line2=lineConvolve(line,kernel,false);
			//printvector(line2);
			//System.out.println();
			iter.putLineFloat(ret,line2, cnt, xdir);
			cnt++;		
		}
		//fp.snapshot();
		fp.setPixels(ret.getPixels());
		//new ImagePlus("cnv1", ret).show();
	}
	
	@Override
	public void convolveFloat1D(ImageStack is, float[] kernel, int xdir) {
		IJLineIteratorStack<float[]> iter= new IJLineIteratorStack<float[]>(is, xdir);
		final int width=is.getWidth();
		final int height=is.getHeight();
		final int depth=is.getSize();
		ImageStack ret=ImageStack.create(width, height, depth, is.getBitDepth());
		int cnt=0;
		while (iter.hasNext()) {
			//System.out.println(" c: "+cnt);
			final float[] line=iter.next();			
			final float[] line2=lineConvolve(line,kernel,false);
			//printvector(line2);
			iter.putLineFloat(ret,line2, cnt, xdir);
			cnt++;		
		}
		
		for (int c=1; c<=depth; c++) {
			Object pixels=ret.getPixels(c);
			is.setPixels(pixels, c);
		}

		
	}
	
	static void printvector(float[] data) {
		for (int i=0; i<data.length; i++) {		
			System.out.print(data[i]+",");		
		}

	}
	
	public final static int Ox=0, Oy=1, Oz=2;
	
	
	
	/** Convolves the image <code>ip</code> with a kernel of width 
	<code>kw</code> and height <code>kh</code>. */
	/**
	 * @param ip
	 * @param kernel
	 * @param kw
	 * @param kh
	 * @param scaled
	 */
	@Override
	public void convolveFloat1D(ImageProcessor ip, float[] kernel, int kw, int kh) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		Rectangle r = ip.getRoi();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = x1 + r.width;
		int y2 = y1 + r.height;
		int uc = kw/2;    
		int vc = kh/2;
		float[] pixels = (float[])ip.getPixels();
		float[] pixels2 = (float[])ip.getPixelsCopy();
 

		boolean vertical = kw==1;

		double sum;
		int offset, i;
		boolean edgePixel;
		int xedge = width-uc;
		int yedge = height-vc;
		for(int y=y1; y<y2; y++) {
			for(int x=x1; x<x2; x++) {
				sum = 0.0;
				i = 0;
				if (vertical) {
					edgePixel = y<vc || y>=yedge;
					offset = x+(y-vc)*width;
					for(int v=-vc; v<=vc; v++) {
						if (edgePixel)
							sum += getPixel(x+uc, y+v, pixels2, width, height)*kernel[i++];
						else
							sum += pixels2[offset+uc]*kernel[i++];
						offset += width;
					}
				} else {
					edgePixel = x<uc || x>=xedge;
					offset = x+(y-vc)*width;
					for(int u = -uc; u<=uc; u++) {
						if (edgePixel)
							sum += getPixel(x+u, y+vc, pixels2, width, height)*kernel[i++];
						else
							sum += pixels2[offset+u]*kernel[i++];
					}
				}
				pixels[x+y*width] = (float)(sum);
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
	
	/* 
	 * computes correlation operation between arrays
	 */
	public static float[] lineConvolve(float[] arr, float[] kernel, boolean flip) {
		if (flip)
			flip(kernel);
		
	
		float[] y= new float[arr.length];
		
		int kw=kernel.length/2;
		//System.out.println("pre loop < "+kw);
		for (int i=0; i<kw; i++) {
			int c=0;
			for (int k=-kw; k<=kw; k++) {
				int q=i-k;
				if (0<=q && q < arr.length) {
					y[i]+=arr[q]*kernel[c];
					c++;
				}
				else{
					
					y[i]+=arr[0]*kernel[c];
					c++;
				}
		
			}			
		}
		// main loop	    
	    for (int i=kw;i<arr.length-kw;i++) {	    	
	        int c=0;
			for (int k=-kw; k<=kw; k++) {
				y[i]+=arr[i-k]*kernel[c];
				c++;
			}
	    }
	    
		//System.out.println("post loop => "+(arr.length-kw));
		for (int i=arr.length-kw; i<arr.length; i++) {
			int c=0;
			for (int k=-kw; k<=kw; k++) {
				int q=i-k;
				if (q < arr.length && 0<=q ) {
					y[i]+=arr[q]*kernel[c];
					c++;
				}
				else {
					y[i]+=arr[arr.length-1]*kernel[c];
					c++;
				}
			}
			
		}
	    return y;
	}
	
	
	 
	private float getPixel(int x, int y, float[] pixels, int width, int height) {
		if (x<=0) x = 0;
		if (x>=width) x = width-1;
		if (y<=0) y = 0;
		if (y>=height) y = height-1;
		return pixels[x+y*width];
	}

	/**
	 * @param fpaux
	 * @param dr
	 * @param d1
	 */
	public static void contrastAdjust(FloatProcessor fpaux, double dr, final double d1) {
		float[] pixels=(float[]) fpaux.getPixels();
		int width=fpaux.getWidth();
		Rectangle rect=fpaux.getRoi();
		for (int i=0; i < pixels.length; i++) {
			final int x=i % width;
			final int y=i / width;
			if (rect.contains(x, y)) {
			pixels[i]= (float) (pixels[i]*dr+d1);
			}
		}
	}
	
	/**
	 * 
	 * @param fp
	 * @return  
	 */
	public static float[] findMinAndMax(FloatProcessor fp) {
		float[] pixels=(float[]) fp.getPixels();
		int width=fp.getWidth();
	 	Rectangle rect=fp.getRoi();
		float min = pixels[0];
		float max =  min;
		for (int i=0; i < pixels.length; i++) {
			final int x=i % width;
			final int y=i / width;
			if (rect.contains(x, y)) {
				float value = pixels[i];
				if (!Float.isInfinite(value)) {
					if (value<min)
						min = value;
					if (value>max)
						max = value;
				}
			}
		}
		
		//System.out.println("min " +min +" max " + max);
		
		return new float[]{min,max}; 
	}
	
	public void cleanup() {
		
	}
	
}
