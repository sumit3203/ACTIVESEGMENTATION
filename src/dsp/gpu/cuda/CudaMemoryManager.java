package dsp.gpu.cuda;

import jcuda.driver.CUdeviceptr;
import jcuda.driver.JCudaDriver;
import java.util.*;

public class CudaMemoryManager {
    private static Map<Long, Queue<CUdeviceptr>> memoryPool = new HashMap<>();
    private static Set<CUdeviceptr> allocatedPointers = new HashSet<>();

    public static synchronized CUdeviceptr allocate(long size) {
        // Check pool for available memory
        Queue<CUdeviceptr> queue = memoryPool.get(size);
        if (queue != null && !queue.isEmpty()) {
            return queue.poll();
        }

        // Allocate new memory
        CUdeviceptr ptr = new CUdeviceptr();
        JCudaDriver.cuMemAlloc(ptr, size);
        allocatedPointers.add(ptr);
        return ptr;
    }

    public static synchronized void free(CUdeviceptr ptr, long size) {
        Queue<CUdeviceptr> queue = memoryPool.get(size);
        if (queue == null) {
            queue = new LinkedList<>();
            memoryPool.put(size, queue);
        }
        queue.offer(ptr);
    }

    public static synchronized void freeAll() {
        for (CUdeviceptr ptr : allocatedPointers) {
            JCudaDriver.cuMemFree(ptr);
        }
        memoryPool.clear();
        allocatedPointers.clear();
    }
}