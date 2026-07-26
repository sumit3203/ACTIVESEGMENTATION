//package dsp;
//
//import dsp.gpu.ConvGpu;
//import dsp.cpu.Conv;
//import dsp.tornado.*;
//import ij.IJ;
//
//public class ConvFactory {
//    private static boolean useGPU = false;
//    private static ConvGpu gpuInstance = null;
//
//    public static IConv createConv() {
//        if (useGPU) {
//            try {
//                if (gpuInstance == null) {
//                    gpuInstance = new ConvGpu();
//                }
//                return gpuInstance;
//            } catch (Exception e) {
//                IJ.log("GPU initialization failed, falling back to CPU: " + e.getMessage());
//                return new Conv();
//            }
//        } else {
//            return new Conv();
//        }
//    }
//
//    public static void cleanup() {
//        if (gpuInstance != null) {
//            gpuInstance.cleanup();
//            gpuInstance = null;
//        }
//    }
//
//    public static void setUseGPU(boolean useGPU) {
//        ConvFactory.useGPU = useGPU;
//    }
//
//    public static boolean isUsingGPU() {
//        return useGPU;
//    }
//}

package dsp;

import dsp.gpu.ConvGpu;
import dsp.cpu.Conv;
import dsp.tornado.ConvTornado;
import ij.IJ;

public class ConvFactory {

	//CH - instead of having two possibilities, added an enum to store three possibilities for now
    // The three possible engines
    public enum Backend { CPU, GPU, TORNADO }

    // Which engine is currently selected (default CPU)
    private static Backend backend = Backend.CPU;

    // Reusable instances (so we don't recreate every time)
    private static ConvGpu gpuInstance = null;
    private static ConvTornado tornadoInstance = null;

    public static IConv createConv() {
    	System.out.println("@@@@@ createConv CALLED, backend=" + backend);
        switch (backend) {

            case GPU:
                try {
                    if (gpuInstance == null) {
                        gpuInstance = new ConvGpu();
                        //CH - added logs for understanding purposes
                        System.out.println(">>> GPU: ConvGPU Instance created successfully");
                    }
                    return gpuInstance;
                } catch (Exception e) {
                	System.out.println("GPU failed, using CPU: " + e.getMessage());
                    return new Conv();
                }

            case TORNADO:
            	 System.out.println("checker_1");
                try {
                    if (tornadoInstance == null) {
                        tornadoInstance = new ConvTornado();
                        System.out.println(">>> Using ConvTornado");
                    }
                    return tornadoInstance;
                } catch (Exception e) {
                	System.out.println("Tornado failed, using CPU: " + e.getMessage());
                    return new Conv();
                }

            case CPU:
            default:
                return new Conv();
        }
    }

    public static void setBackend(Backend b) {
        backend = b;
    }

    public static Backend getBackend() {
        return backend;
    }

    public static void cleanup() {
        if (gpuInstance != null) {
            gpuInstance.cleanup();
            gpuInstance = null;
        }
        if (tornadoInstance != null) {
            tornadoInstance.cleanup();
            tornadoInstance = null;
        }
    }
    // CH - rewritten the following functions to reroute to tornado insteead of jcuda, and to read the value of backend to answer
    public static void setUseGPU(boolean useGPU) {
    	backend=useGPU? Backend.TORNADO : Backend.CPU;
    }
    
    public static boolean isUsingGPU() {
    	return backend != Backend.CPU;
    }
    
}