package ijaux.scale;

import java.lang.reflect.Array;

public class SortUtil {
	private SortUtil() {}


	/*
	 * swap - true - physical sorting; false - only sorted indices are returned
	 */
	public static <V> int[] quicksort(V main, boolean swap) {
		boolean isArray=main.getClass().isArray();
		if (!isArray)
			throw new IllegalArgumentException("not an array type");
		int l=Array.getLength(main);
		int[] index = new int[l];
		for (int i=0; i<l; i++) {
			index[i]=i;
		}
		quicksort_swp(main, index, 0, index.length - 1, swap);

		return index;
	}

	// quicksort a[left] to a[right]
	public static <V> void quicksort_swp(V a, int[] index, int left, int right, boolean swap) {
		if (right <= left) return;
		int i=0;
		if (a instanceof float[])
			i = partition_swp((float[]) a, index, left, right, swap);
		if (a instanceof double[])
			i = partition_swp((double[]) a, index, left, right, swap);
		if (a instanceof int[])
			i = partition_swp((int[]) a, index, left, right, swap);
		if (a instanceof short[])
			i = partition_swp((short[]) a, index, left, right, swap);
		if (a instanceof byte[])
			i = partition_swp((byte[]) a, index, left, right, swap);
		quicksort_swp(a, index, left, i-1, swap);
		quicksort_swp(a, index, i+1, right, swap);
	}



	// partition a[left] to a[right], assumes left < right
	private static int partition_swp(float[] a, int[] index, 
			int left, int right, boolean swap) {
		int i = left - 1;
		int j = right;
		if (swap) {
			while (true) {
				while ( a[++i]< a[right])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[right] < a[--j])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;   
				// check if pointers cross
				float s = a[i];
				a[i] = a[j];
				a[j] = s;
				exch_i(index, i, j);               // swap two elements into place
			}
			float s = a[i];
			a[i] = a[right];
			a[right] = s;
			exch_i(index, i, right);               // swap with partition element
		} else {
			while (true) {
				while ( a[index[++i]] < a[index[right]])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[index[right]] < a[index[--j]])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;                  // check if pointers cross
				exch_i(index, i, j);               // swap two elements into place
			}
			exch_i(index, i, right);               // swap with partition element
		}

		return i;
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition_swp(double[] a, int[] index, 
			int left, int right, boolean swap) {
		int i = left - 1;
		int j = right;
		if (swap) {
			while (true) {
				while ( a[++i]< a[right])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[right] < a[--j])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;   
				// check if pointers cross
				double s = a[i];
				a[i] = a[j];
				a[j] = s;
				exch_i(index, i, j);               // swap two elements into place
			}
			double s = a[i];
			a[i] = a[right];
			a[right] = s;
			exch_i(index, i, right);               // swap with partition element
		} else {
			while (true) {
				while ( a[index[++i]] < a[index[right]])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[index[right]] < a[index[--j]])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;                  // check if pointers cross
				exch_i(index, i, j);               // swap two elements into place
			}
			exch_i(index, i, right);               // swap with partition element
		}

		return i;
	}


	// partition a[left] to a[right], assumes left < right
	private static int partition_swp(int[] a, int[] index, 
			int left, int right, boolean swap) {
		int i = left - 1;
		int j = right;
		if (swap) {
			while (true) {
				while ( a[++i]< a[right])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[right] < a[--j])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;   
				// check if pointers cross
				int s = a[i];
				a[i] = a[j];
				a[j] = s;
				exch_i(index, i, j);               // swap two elements into place
			}
			int s = a[i];
			a[i] = a[right];
			a[right] = s;
			exch_i(index, i, right);               // swap with partition element
		} else {
			while (true) {
				while ( a[index[++i]] < a[index[right]])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[index[right]] < a[index[--j]])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;                  // check if pointers cross
				exch_i(index, i, j);               // swap two elements into place
			}
			exch_i(index, i, right);               // swap with partition element
		}

		return i;
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition_swp(short[] a, int[] index, 
			int left, int right, boolean swap) {
		int i = left - 1;
		int j = right;
		if (swap) {
			while (true) {
				while ( a[++i]< a[right])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[right] < a[--j])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;   
				// check if pointers cross
				short s = a[i];
				a[i] = a[j];
				a[j] = s;
				exch_i(index, i, j);               // swap two elements into place
			}
			short s = a[i];
			a[i] = a[right];
			a[right] = s;
			exch_i(index, i, right);               // swap with partition element
		} else {
			while (true) {
				while ( a[index[++i]] < a[index[right]])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[index[right]] < a[index[--j]])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;                  // check if pointers cross
				exch_i(index, i, j);               // swap two elements into place
			}
			exch_i(index, i, right);               // swap with partition element
		}

		return i;
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition_swp(byte[] a, int[] index, 
			int left, int right, boolean swap) {
		int i = left - 1;
		int j = right;
		if (swap) {
			while (true) {
				while ( a[++i]< a[right])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[right] < a[--j])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;   
				// check if pointers cross
				byte s = a[i];
				a[i] = a[j];
				a[j] = s;
				exch_i(index, i, j);               // swap two elements into place
			}
			byte s = a[i];
			a[i] = a[right];
			a[right] = s;
			exch_i(index, i, right);               // swap with partition element
		} else {
			while (true) {
				while ( a[index[++i]] < a[index[right]])      // find item on left to swap
					;                               // a[right] acts as sentinel
				while ( a[index[right]] < a[index[--j]])      // find item on right to swap
					if (j == left) break;           // don't go out-of-bounds
				if (i >= j) break;                  // check if pointers cross
				exch_i(index, i, j);               // swap two elements into place
			}
			exch_i(index, i, right);               // swap with partition element
		}

		return i;
	}

	// exchange a[i] and a[j]
	private static void exch_i( int[] index, int i, int j) {
		int b = index[i];
		index[i] = index[j];
		index[j] = b;
	}


}
