// test_kernel.cu

// -------------------------------
// This file contains two CUDA kernel functions:
// 1. helloKernel - prints a message from each GPU thread.
// 2. writeTest   - writes data into an output array from the GPU.
// These functions are compiled into a .ptx file and called from Java using JCuda.
// -------------------------------

// `extern "C"` prevents C++ name mangling so that JCuda (which uses C-style strings) can find the kernel function by name.
extern "C"

// This kernel will just print from each thread.
// Used to test if kernel is launching correctly.
__global__ void helloKernel() {
	printf("Hello from GPU thread %d\n", threadIdx.x);
}

extern "C"

// This kernel will write values to a GPU array,
// so we can copy it back in Java and confirm it worked.
__global__ void writeTest(int *output) {
	// Get the thread index in the current block.
	int tid = threadIdx.x;

	// Each thread writes its index * 10 into the output array.
	// Example: thread 0 → 0, thread 1 → 10, thread 2 → 20, etc.
	output[tid] = tid * 10;
}
