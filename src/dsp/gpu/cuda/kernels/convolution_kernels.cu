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

extern "C"
__global__ void biLaplacianKernel(float* input, float* output,
	int width, int height, float sigma) {
	int i = blockIdx.y * blockDim.y + threadIdx.y;
	int j = blockIdx.x * blockDim.x + threadIdx.x;

	if (i < height && j < width) {
		// Center coordinates
		float x = j - width / 2.0f;
		float y = i - height / 2.0f;
		float r2 = (x*x + y * y) / (sigma*sigma);

		// Bi-Laplacian of Gaussian formula
		float biLogVal = (r2*r2 - 6.0f*r2 + 3.0f) * exp(-r2 / 2.0f) /
			(3.0f * 3.141592653589793f * sigma*sigma*sigma*sigma);

		// Apply to input pixel
		output[i * width + j] = biLogVal * input[i * width + j];
	}
}

extern "C"
__global__ void gaussianDerivativeKernel(float* input, float* output,
	int width, int height, float sigma,
	int orderX, int orderY) {
	int i = blockIdx.y * blockDim.y + threadIdx.y;
	int j = blockIdx.x * blockDim.x + threadIdx.x;

	if (i < height && j < width) {
		float x = j - width / 2.0f;
		float y = i - height / 2.0f;

		// Gaussian derivative implementation
		// This is a placeholder - you'll need to implement the specific
		// Gaussian derivative formulas based on orderX and orderY
		float value = input[i * width + j];

		// Simple example: first derivative in x
		if (orderX == 1 && orderY == 0) {
			float gaussian = exp(-(x*x + y * y) / (2.0f * sigma*sigma));
			float derivative = -x / (sigma*sigma) * gaussian;
			output[i * width + j] = derivative * value;
		}
		// Add more cases for different derivative orders
		else {
			output[i * width + j] = value;
		}
	}
}