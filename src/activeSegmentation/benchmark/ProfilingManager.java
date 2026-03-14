package activeSegmentation.benchmark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 ProfilingManager
 Upgraded to support CPU vs GPU comparison.
 Keeps track of the best/latest timing for both modes per filter.
 */
public class ProfilingManager {

    public static class ComparisonRecord {
        public final String filterName;
        public long cpuTime = -1; // -1 indicates no data
        public long gpuTime = -1;

        public ComparisonRecord(String filterName) {
            this.filterName = filterName;
        }

        public String getSpeedup() {
            if (cpuTime <= 0 || gpuTime <= 0) return "N/A";
            double speedup = (double) cpuTime / gpuTime;
            return String.format("%.2fx", speedup);
        }
    }

    // Use LinkedHashMap to maintain filter insertion order
    private static final Map<String, ComparisonRecord> records = new LinkedHashMap<>();

    /**
     * Records a timing result for a specific mode. 
     * If a record for this filter already exists, it updates the specific mode's time.
     */
    public static synchronized void record(String filterName, String mode, long timeMs) {
        ComparisonRecord record = records.getOrDefault(filterName, new ComparisonRecord(filterName));
        if ("GPU".equalsIgnoreCase(mode)) {
            record.gpuTime = timeMs;
        } else {
            record.cpuTime = timeMs;
        }
        records.put(filterName, record);
    }

    public static synchronized List<ComparisonRecord> getComparisonResults() {
        return new ArrayList<>(records.values());
    }

    public static synchronized void clear() {
        records.clear();
    }

    /**
     * Check if we have at least one valid result
     */
    public static synchronized boolean hasData() {
        return !records.isEmpty();
    }
}
