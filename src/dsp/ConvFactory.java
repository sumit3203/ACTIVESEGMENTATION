package dsp;

import dsp.gpu.ConvGpu;
import dsp.cpu.Conv;
import ij.IJ;

public class ConvFactory {
    private static boolean useGPU = false;
    private static ConvGpu gpuInstance = null;

    public static IConv createConv() {
        if (useGPU) {
            try {
                if (gpuInstance == null) {
                    gpuInstance = new ConvGpu();
                }
                return gpuInstance;
            } catch (Exception e) {
                IJ.log("GPU initialization failed, falling back to CPU: " + e.getMessage());
                return new Conv();
            }
        } else {
            return new Conv();
        }
    }

    public static void cleanup() {
        if (gpuInstance != null) {
            gpuInstance.cleanup();
            gpuInstance = null;
        }
    }

    public static void setUseGPU(boolean useGPU) {
        ConvFactory.useGPU = useGPU;
    }

    public static boolean isUsingGPU() {
        return useGPU;
    }
}