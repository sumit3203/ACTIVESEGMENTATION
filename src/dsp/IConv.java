package dsp;

import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public interface IConv {
    void convolveSemiSep(FloatProcessor ip, float[] kernx, float[] kern_diff);
    void convolveSemiSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff);
    void convolveSepIter(FloatProcessor ip, float[] kernx, float[] kern_diff);
    void convolveSep(ImageProcessor ip, float[] kernx, float[] kern_diff);
    void convolveSemiSep(ImageStack xstack, float[] kernx, float[] kerny, float[] kernz);
    void convolveSep3D(ImageStack xstack, float[] kernx, float[] kern_diffx, float[] kernz);
    boolean convolveFloat(ImageProcessor ip, float[] kernel, int kw, int kh);
    void convolveFloat1D(FloatProcessor fp, float[] kernel, int xdir);
    void convolveFloat1D(ImageStack is, float[] kernel, int xdir);
    void convolveFloat1D(ImageProcessor ip, float[] kernel, int kw, int kh);
}