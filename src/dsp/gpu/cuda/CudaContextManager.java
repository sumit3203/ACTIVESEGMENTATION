package dsp.gpu.cuda;

import jcuda.driver.*;

public class CudaContextManager {
    private static CUcontext context = null;

    static {
        JCudaDriver.setExceptionsEnabled(true);
        JCudaDriver.cuInit(0);
    }

    public static CUcontext getContext() {
        if (context == null) {
            // Create a new CUDA context
            CUdevice device = new CUdevice();
            JCudaDriver.cuDeviceGet(device, 0); // Use the first GPU device
            context = new CUcontext();
            JCudaDriver.cuCtxCreate(context, 0, device);
        }
        return context;
    }

    public static void destroyContext() {
        if (context != null) {
            JCudaDriver.cuCtxDestroy(context);
            context = null;
        }
    }
}