package activeSegmentation.evaluation;

import ijaux.scale.Pair;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.ToIntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NPArray {
    public static Pair<double[], int[]> sort(double[] a, boolean ascending) {
        int[] indexes = new int[a.length];
        double[] sortedArray = new double[a.length];
        indexes = NPArray.argsort(a, ascending);
        int i = 0;
        while (i < indexes.length) {
            sortedArray[i] = a[indexes[i]];
            ++i;
        }
        return new Pair<double[], int[]>(sortedArray, indexes);
    }

    public static double[] intTodouble(int[] ints) {
        return Arrays.stream(ints).asDoubleStream().toArray();
    }

    public static int[] argsort(final double[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        int i = 0;
        while (i < indexes.length) {
            indexes[i] = i;
            ++i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
            }
        });
        int[] ret = new int[indexes.length];
        int i2 = 0;
        while (i2 < ret.length) {
            ret[i2] = indexes[i2];
            ++i2;
        }
        return ret;
    }

    public static double[] sortByIndex(double[] a, int[] indexes) {
        double[] sortedArray = new double[a.length];
        int i = 0;
        while (i < indexes.length) {
            sortedArray[i] = a[indexes[i]];
            ++i;
        }
        return sortedArray;
    }

    public static double[] diff(double[] a) {
        double[] diffArray = new double[a.length - 1];
        int i = 0;
        while (i < diffArray.length) {
            diffArray[i] = a[i + 1] - a[i];
            ++i;
        }
        return diffArray;
    }

    public static double[] diff(double[] a, double[] b) {
        double[] diffArray = new double[a.length];
        int i = 0;
        while (i < diffArray.length) {
            diffArray[i] = a[i] - b[i];
            ++i;
        }
        return diffArray;
    }

    public static double[] addScaler(double[] a, double scaler) {
        double[] scallerA = new double[a.length];
        int i = 0;
        while (i < scallerA.length) {
            scallerA[i] = a[i] + scaler;
            ++i;
        }
        return scallerA;
    }

    public static int[] addScaler(int[] a, int scaler) {
        int[] scallerA = new int[a.length];
        int i = 0;
        while (i < scallerA.length) {
            scallerA[i] = a[i] + scaler;
            ++i;
        }
        return scallerA;
    }

    public static double[] remove(double[] a, double element) {
        double[] newArray = new double[a.length];
        System.arraycopy(a, 0, newArray, 0, a.length);
        int length = a.length;
        if (length == 0) {
            return a;
        }
        int i = 0;
        int k = 0;
        int j = 0;
        while (j < length) {
            if (newArray[j] != element) {
                newArray[i] = newArray[j];
                ++i;
            } else {
                ++k;
            }
            ++j;
        }
        System.out.println(k);
        double[] selected = new double[a.length - k];
        System.arraycopy(newArray, 0, selected, 0, a.length - k);
        return selected;
    }

    public static int[] where(double[] a, double element) {
        ArrayList<Integer> number = new ArrayList<Integer>();
        int j = 0;
        while (j < a.length) {
            if (a[j] != element) {
                number.add(j);
            }
            ++j;
        }
        int[] indexes = new int[number.size()];
        int i=0;
        for(Integer value: indexes) {
        	indexes[i]=value;
        	i++;
        }
        return indexes;
    }

	public static int[] r_(int[] a, int[] b){
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
 
    public static double[] cumSum(double[] a) {
        double[] cumSum = new double[a.length];
        cumSum[0] = a[0];
        int i = 1;
        while (i < cumSum.length) {
            cumSum[i] = cumSum[i - 1] + a[i];
            ++i;
        }
        return cumSum;
    }

    public static double[] sliceByIndices(double[] a, int[] indices) {
        double[] slice = new double[indices.length];
        int i = 0;
        while (i < indices.length) {
            slice[i] = a[indices[i]];
            ++i;
        }
        return slice;
    }

    public static double[] normalize(double[] a, double number) {
        double[] normalize = new double[a.length];
        int i = 0;
        while (i < normalize.length) {
            normalize[i] = a[i] / number;
            ++i;
        }
        return normalize;
    }
}