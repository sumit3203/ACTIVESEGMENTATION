/**
 * convolution_kernels.cu
 * Contains both 2D and 1D convolution kernels for image processing
 */

extern "C"
__global__ void convolve2DKernel(float* input, float* kernel, float* output,
	int width, int height, int kernelSize) {
	int i = blockIdx.y * blockDim.y + threadIdx.y;
	int j = blockIdx.x * blockDim.x + threadIdx.x;

	if (i < height && j < width) {
		float sum = 0;
		int halfKernel = kernelSize / 2;

		for (int ki = 0; ki < kernelSize; ki++) {
			for (int kj = 0; kj < kernelSize; kj++) {
				int ii = i + ki - halfKernel;
				int jj = j + kj - halfKernel;

				// Boundary handling with clamp-to-edge
				if (ii >= 0 && ii < height && jj >= 0 && jj < width) {
					sum += input[ii * width + jj] * kernel[ki * kernelSize + kj];
				}
			}
		}
		output[i * width + j] = sum;
	}
}

extern "C"
__global__ void convolve1DKernel(float* input, float* kernel, float* output,
	int length, int kernelSize) {
	int idx = blockIdx.x * blockDim.x + threadIdx.x;

	if (idx < length) {
		float sum = 0.0f;
		int halfKernel = kernelSize / 2;

		for (int k = -halfKernel; k <= halfKernel; k++) {
			int pos = idx + k;
			float value;

			// Handle boundaries with clamp-to-edge
			if (pos < 0) {
				value = input[0];
			}
			else if (pos >= length) {
				value = input[length - 1];
			}
			else {
				value = input[pos];
			}

			sum += value * kernel[k + halfKernel];
		}

		output[idx] = sum;
	}
}
