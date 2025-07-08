package jcuda;

import jcuda.driver.*;

import static jcuda.driver.JCudaDriver.*;

public class JCudaTest {
    public static void main(String[] args) {
        // Enable exception handling and initialize CUDA driver
        JCudaDriver.setExceptionsEnabled(true);
        cuInit(0);
        System.out.println("1. JCuda initialized.");

        // Get the first GPU device and create a context
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);
        System.out.println("2. CUDA context created.");

        // Load the compiled PTX file
        CUmodule module = new CUmodule();
        cuModuleLoad(module, "jcuda/cuda/test_kernel.ptx");
        System.out.println("3. PTX kernel file loaded.");

        // Launch helloKernel()
        CUfunction helloFunction = new CUfunction();
        cuModuleGetFunction(helloFunction, module, "helloKernel");

        System.out.println("\nLaunching helloKernel with 5 threads...");
        cuLaunchKernel(helloFunction,
                1, 1, 1,     // Grid size
                5, 1, 1,     // Block size
                0, null,
                Pointer.to(), null);
        cuCtxSynchronize();
        System.out.println("helloKernel launched.");

        // Launch writeTest()
        CUfunction writeFunction = new CUfunction();
        cuModuleGetFunction(writeFunction, module, "writeTest");

        // Prepare a device array and host array for 10 integers
        int numElements = 10;
        int size = numElements * Sizeof.INT;
        int[] hostOutput = new int[numElements];
        CUdeviceptr deviceOutput = new CUdeviceptr();
        cuMemAlloc(deviceOutput, size);

        // Set kernel parameters for writeTest
        Pointer kernelParams = Pointer.to(Pointer.to(deviceOutput));

        System.out.println("\nLaunching writeTest with 10 threads...");
        cuLaunchKernel(writeFunction,
                1, 1, 1,
                numElements, 1, 1,
                0, null,
                kernelParams, null);
        cuCtxSynchronize();

        // Copy results back from GPU to host
        cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput, size);

        // Print the results
        System.out.println("Result from GPU (writeTest):");
        for (int i = 0; i < numElements; i++) {
            System.out.println("hostOutput[" + i + "] = " + hostOutput[i]);
        }

        // Free GPU memory
        cuMemFree(deviceOutput);
        System.out.println("\nJCuda test completed successfully.");

    }
}
