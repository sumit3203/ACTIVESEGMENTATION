package activeSegmentation;

import java.awt.Image;

public class FilterBenchmark {
    public static double getExecutionTime(IFilter filter, Image input) {
        for (int i = 0; i < 5; i++) {
            filter.applyFilter(input);
        }
        long start = System.nanoTime();
        filter.applyFilter(input);
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0;
    }
}
