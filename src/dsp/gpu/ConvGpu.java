package dsp.gpu;

import dsp.IConv;
import dsp.gpu.cuda.CudaContextManager;
import dsp.gpu.cuda.CudaMemoryManager;
import ij.IJ;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.IJLineIteratorIP;
import ijaux.scale.IJLineIteratorStack;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;

import java.awt.*;
import java.io.File;

public class ConvGpu implements IConv {

    public static boolean debug = false;
    public final static int Ox = 0, Oy = 1, Oz = 2;

    private CUmodule module;
    private CUfunction function2D;
    private CUfunction function1D;
    private boolean gpuInitialized = false;
    private CUstream stream;

    static {
        JCudaDriver.setExceptionsEnabled(true);
        JCudaDriver.cuInit(0);
    }

    public ConvGpu() {
        try {
            // Initialize CUDA context
            CudaContextManager.getContext();

            // Load PTX file
            String ptxFileName = "convolution_kernels.ptx";
            File ptxFile = new File(ptxFileName);
            if (!ptxFile.exists()) {
                throw new RuntimeException("PTX file not found: " + ptxFile.getAbsolutePath());
            }

            // Load module
            module = new CUmodule();
            JCudaDriver.cuModuleLoad(module, ptxFileName);

            // Get all kernel functions
            function2D = new CUfunction();
            JCudaDriver.cuModuleGetFunction(function2D, module, "convolve2DKernel");

            function1D = new CUfunction();
            JCudaDriver.cuModuleGetFunction(function1D, module, "convolve1DKernel");

            // Create CUDA stream
            stream = new CUstream();
            JCudaDriver.cuStreamCreate(stream, CUstream_flags.CU_STREAM_DEFAULT);

            gpuInitialized = true;
        } catch (Exception e) {
            IJ.log("GPU initialization failed: " + e.getMessage());
            gpuInitialized = false;
        }
    }

    @Override
	public void convolveSemiSep(FloatProcessor ip, float[] kernx, float[] kern_diff) {
        FloatProcessor ip2 = null;
        FloatProcessor ipx = null;
        final Rectangle roi = ip.getRoi();

        synchronized (this) {
            ip2 = (FloatProcessor) ip.duplicate();
            ip2.setRoi(roi);
            ip2.setSnapshotPixels(ip.getSnapshotPixels());
            ipx = (FloatProcessor) ip2.duplicate();
            ipx.setRoi(roi);
            ipx.setSnapshotPixels(ip.getSnapshotPixels());
        }

        convolveFloat1D(ipx, kern_diff, kern_diff.length, 1); // x direction
        ipx.setSnapshotPixels(null);
        convolveFloat1D(ipx, kernx, 1, kernx.length); // y direction

        convolveFloat1D(ip2, kernx, kernx.length, 1); // x direction
        ip2.setSnapshotPixels(null);
        convolveFloat1D(ip2, kern_diff, 1, kern_diff.length); // y direction
        add(ip2, ipx, ip2.getRoi());
        ip.setPixels(ip2.getPixels());
    }

    @Override
	public void convolveSemiSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff) {
        FloatProcessor ip2 = (FloatProcessor) ip.duplicate();
        FloatProcessor ipx = (FloatProcessor) ip.duplicate();
        final Rectangle roi = ip.getRoi();

        ip2.setRoi(roi);
        ipx.setRoi(roi);

        convolveFloat1D(ipx, kern_diff, Ox); // x direction
        convolveFloat1D(ipx, kernx, Oy); // y direction

        convolveFloat1D(ip2, kernx, Ox); // x direction
        convolveFloat1D(ip2, kern_diff, Oy); // y direction

        add(ip2, ipx, ip.getRoi());
        ip.setPixels(ip2.getPixels());
    }

    @Override
	public void convolveSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff) {
        convolveFloat1D(ip, kern_diff, Ox); // x direction
        convolveFloat1D(ip, kernx, Oy); // y direction
    }

    @Override
	public void convolveSep(ImageProcessor ip, float[] kernx, float[] kern_diff) {
        convolveFloat1D(ip, kern_diff, kern_diff.length, 1); // x direction
        convolveFloat1D(ip, kernx, 1, kernx.length); // y direction
    }

    @Override
	public void convolveSemiSep(ImageStack xstack, float[] kernx, float[] kerny, float[] kernz) {
        long time = -System.nanoTime();
        ImageStack ystack = cloneStack(xstack);
        ImageStack zstack = cloneStack(xstack);

        time += System.nanoTime();
        time /= 1000.0f;
        time = -System.nanoTime();

        convolveFloat1D(xstack, kernx, Ox); // X
        convolveFloat1D(xstack, kerny, Oy); // Y
        convolveFloat1D(xstack, kernz, Oz); // Z

        convolveFloat1D(ystack, kernx, Oy); // Y
        convolveFloat1D(ystack, kerny, Ox); // X
        convolveFloat1D(ystack, kernz, Oz); // Z

        convolveFloat1D(zstack, kernx, Oz); // Z
        convolveFloat1D(zstack, kerny, Ox); // X
        convolveFloat1D(zstack, kernz, Oy); // Y

        addToStack(xstack, ystack, zstack);
        ystack = null;
        zstack = null;

        time += System.nanoTime();
        time /= 1000.0f;
        System.out.println("processing time: " + time + " us");
    }

    @Override
	public void convolveSep3D(ImageStack xstack, float[] kernx, float[] kern_diffx, float[] kernz) {
        convolveFloat1D(xstack, kern_diffx, Ox);
        convolveFloat1D(xstack, kernx, Oy);
        convolveFloat1D(xstack, kernz, Oz);
    }

    private void addToStack(ImageStack dest, ImageStack a, ImageStack b) {
        int bitdepth = dest.getBitDepth();
        if (bitdepth != a.getBitDepth() || a.getBitDepth() != b.getBitDepth())
            return;

        final int sz = dest.getSize();
        for (int i = 1; i <= sz; i++) {
            switch (bitdepth) {
                case 8: {
                    byte[] pixels = (byte[]) dest.getPixels(i);
                    byte[] pixels_a = (byte[]) a.getPixels(i);
                    byte[] pixels_b = (byte[]) b.getPixels(i);
                    for (int c = 0; c < pixels.length; c++)
                        pixels[c] += pixels_a[c] + pixels_b[c];
                    break;
                }
                case 16: {
                    short[] pixels = (short[]) dest.getPixels(i);
                    short[] pixels_a = (short[]) a.getPixels(i);
                    short[] pixels_b = (short[]) b.getPixels(i);
                    for (int c = 0; c < pixels.length; c++)
                        pixels[c] += pixels_a[c] + pixels_b[c];
                    break;
                }
                case 24: {
                    int[] pixels = (int[]) dest.getPixels(i);
                    int[] pixels_a = (int[]) a.getPixels(i);
                    int[] pixels_b = (int[]) b.getPixels(i);
                    for (int c = 0; c < pixels.length; c++)
                        pixels[c] += pixels_a[c] + pixels_b[c];
                    break;
                }
                case 32: {
                    float[] pixels = (float[]) dest.getPixels(i);
                    float[] pixels_a = (float[]) a.getPixels(i);
                    float[] pixels_b = (float[]) b.getPixels(i);
                    for (int c = 0; c < pixels.length; c++)
                        pixels[c] += pixels_a[c] + pixels_b[c];
                    break;
                }
            }
        }
    }

    public static ImageStack cloneStack(ImageStack is) {
        final int width = is.getWidth();
        final int height = is.getHeight();
        Object[] array = is.getImageArray();
        ImageStack ret = ImageStack.create(width, height, array.length, is.getBitDepth());
        Object[] array2 = array.clone();
        int cnt = 1;
        for (Object o : array2)
            ret.setPixels(o, cnt++);
        ret.update(is.getProcessor(1));
        ret.setRoi(is.getRoi());
        return ret;
    }

    private void add(ImageProcessor dest, ImageProcessor src, Rectangle r) {
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                float sum = dest.getf(x, y) + src.getf(x, y);
                dest.setf(x, y, sum);
            }
        }
    }

    @Override
	public boolean convolveFloat(ImageProcessor ip, float[] kernel, int kw, int kh) {
        if (!gpuInitialized) {
            IJ.log("GPU not initialized - cannot perform convolution");
            return false;
        }
        return convolveFloatGPU(ip, kernel, kw, kh);
    }

    private boolean convolveFloatGPU(ImageProcessor ip, float[] kernel, int kw, int kh) {
        long startTime = System.nanoTime();
        int width = ip.getWidth();
        int height = ip.getHeight();
        float[] input = (float[]) ip.getPixels();
        float[] output = new float[input.length];

        try {
            // Use memory pool
            long inputSize = input.length * Sizeof.FLOAT;
            long kernelSize = kernel.length * Sizeof.FLOAT;
            long outputSize = output.length * Sizeof.FLOAT;

            CUdeviceptr d_input = CudaMemoryManager.allocate(inputSize);
            CUdeviceptr d_kernel = CudaMemoryManager.allocate(kernelSize);
            CUdeviceptr d_output = CudaMemoryManager.allocate(outputSize);

            // Use async memory copies with stream
            JCudaDriver.cuMemcpyHtoDAsync(d_input, Pointer.to(input), inputSize, stream);
            JCudaDriver.cuMemcpyHtoDAsync(d_kernel, Pointer.to(kernel), kernelSize, stream);

            // Setup kernel parameters
            Pointer kernelParameters = Pointer.to(
                    Pointer.to(d_input),
                    Pointer.to(d_kernel),
                    Pointer.to(d_output),
                    Pointer.to(new int[]{width}),
                    Pointer.to(new int[]{height}),
                    Pointer.to(new int[]{kw})
            );

            // Launch kernel with stream
            int blockSize = 16;
            int gridX = (width + blockSize - 1) / blockSize;
            int gridY = (height + blockSize - 1) / blockSize;

            JCudaDriver.cuLaunchKernel(function2D,
                    gridX, gridY, 1,
                    blockSize, blockSize, 1,
                    0, stream, kernelParameters, null);

            // Async copy back to host
            JCudaDriver.cuMemcpyDtoHAsync(Pointer.to(output), d_output, outputSize, stream);

            // Wait for all async operations to complete
            JCudaDriver.cuStreamSynchronize(stream);

            // Set the result
            ip.setPixels(output);

            // Return memory to pool
            CudaMemoryManager.free(d_input, inputSize);
            CudaMemoryManager.free(d_kernel, kernelSize);
            CudaMemoryManager.free(d_output, outputSize);

            long endTime = System.nanoTime();
            IJ.log(String.format("GPU convolution time: %.2f ms", (endTime - startTime) / 1e6));

            return true;
        } catch (Exception e) {
            IJ.log("GPU convolution failed: " + e.getMessage());
            return false;
        }
    }

    public void cleanup() {
        try {
            if (stream != null) {
                JCudaDriver.cuStreamDestroy(stream);
            }
            // Free all pooled memory at application exit
            CudaMemoryManager.freeAll();
        } catch (Exception e) {
            IJ.log("GPU cleanup failed: " + e.getMessage());
        }
    }

    @Override
	public void convolveFloat1D(FloatProcessor fp, float[] kernel, int xdir) {
        IJLineIteratorIP<float[]> iter = new IJLineIteratorIP<float[]>(fp, xdir);
        final int width = fp.getWidth();
        final int height = fp.getHeight();
        FloatProcessor ret = new FloatProcessor(width, height);

        int cnt = 0;
        if (debug) {
            printvector(kernel);
            System.out.println();
        }
        while (iter.hasNext()) {
            final float[] line = iter.next();
            final float[] line2 = lineConvolveGPU(line, kernel, false);
            iter.putLineFloat(ret, line2, cnt, xdir);
            cnt++;
        }
        fp.setPixels(ret.getPixels());
    }

    @Override
	public void convolveFloat1D(ImageStack is, float[] kernel, int xdir) {
        IJLineIteratorStack<float[]> iter = new IJLineIteratorStack<float[]>(is, xdir);
        final int width = is.getWidth();
        final int height = is.getHeight();
        final int depth = is.getSize();
        ImageStack ret = ImageStack.create(width, height, depth, is.getBitDepth());
        int cnt = 0;
        while (iter.hasNext()) {
            final float[] line = iter.next();
            final float[] line2 = lineConvolveGPU(line, kernel, false);
            iter.putLineFloat(ret, line2, cnt, xdir);
            cnt++;
        }

        for (int c = 1; c <= depth; c++) {
            Object pixels = ret.getPixels(c);
            is.setPixels(pixels, c);
        }
    }

    @Override
	public void convolveFloat1D(ImageProcessor ip, float[] kernel, int kw, int kh) {
        convolveFloatGPU(ip, kernel, kw, kh);
    }

    static void printvector(float[] data) {
        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + ",");
        }
    }

    public static float[] lineConvolveGPU(float[] arr, float[] kernel, boolean flip) {
        // This should be implemented using GPU kernels
        // For now, we'll just call the CPU version (you should replace this)
        return lineConvolve(arr, kernel, flip);
    }

    private static float[] lineConvolve(float[] arr, float[] kernel, boolean flip) {
        if (flip)
            flip(kernel);

        float[] y = new float[arr.length];
        int kw = kernel.length / 2;

        for (int i = 0; i < kw; i++) {
            int c = 0;
            for (int k = -kw; k <= kw; k++) {
                int q = i - k;
                if (0 <= q && q < arr.length) {
                    y[i] += arr[q] * kernel[c];
                } else {
                    y[i] += arr[0] * kernel[c];
                }
                c++;
            }
        }

        for (int i = kw; i < arr.length - kw; i++) {
            int c = 0;
            for (int k = -kw; k <= kw; k++) {
                y[i] += arr[i - k] * kernel[c];
                c++;
            }
        }

        for (int i = arr.length - kw; i < arr.length; i++) {
            int c = 0;
            for (int k = -kw; k <= kw; k++) {
                int q = i - k;
                if (q < arr.length && 0 <= q) {
                    y[i] += arr[q] * kernel[c];
                } else {
                    y[i] += arr[arr.length - 1] * kernel[c];
                }
                c++;
            }
        }
        return y;
    }

    public static void flip(float[] kernel) {
        final int s = kernel.length - 1;
        for (int i = 0; i < kernel.length / 2; i++) {
            final float c = kernel[i];
            kernel[i] = kernel[s - i];
            kernel[s - i] = c;
        }
    }

    public static void contrastAdjust(FloatProcessor fpaux, double dr, final double d1) {
        float[] pixels = (float[]) fpaux.getPixels();
        int width = fpaux.getWidth();
        Rectangle rect = fpaux.getRoi();
        for (int i = 0; i < pixels.length; i++) {
            final int x = i % width;
            final int y = i / width;
            if (rect.contains(x, y)) {
                pixels[i] = (float) (pixels[i] * dr + d1);
            }
        }
    }

    public static float[] findMinAndMax(FloatProcessor fp) {
        float[] pixels = (float[]) fp.getPixels();
        int width = fp.getWidth();
        Rectangle rect = fp.getRoi();
        float min = pixels[0];
        float max = min;
        for (int i = 0; i < pixels.length; i++) {
            final int x = i % width;
            final int y = i / width;
            if (rect.contains(x, y)) {
                float value = pixels[i];
                if (!Float.isInfinite(value)) {
                    if (value < min)
                        min = value;
                    if (value > max)
                        max = value;
                }
            }
        }
        return new float[]{min, max};
    }
}