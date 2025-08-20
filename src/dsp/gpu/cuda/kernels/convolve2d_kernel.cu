// convolve2d_kernel.cu

extern "C"
__global__ void convolve2DKernel(float* input, float* kernel, float* output, int width, int height, int kernelSize) {
	int i = blockIdx.y * blockDim.y + threadIdx.y;
	int j = blockIdx.x * blockDim.x + threadIdx.x;

	if (i < height && j < width) {
		float sum = 0;
		for (int ki = 0; ki < kernelSize; ki++) {
			for (int kj = 0; kj < kernelSize; kj++) {
				int ii = i + ki - kernelSize / 2;
				int jj = j + kj - kernelSize / 2;
				if (ii >= 0 && ii < height && jj >= 0 && jj < width) {
					sum += input[ii * width + jj] * kernel[ki * kernelSize + kj];
				}
			}
		}
		output[i * width + j] = sum;
	}
}
