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
	
	// CH - ADD: parity record (mirrors ComparisonRecord's shape) ----
//	public static class ParityRecord {
//	    public final String filterName;
//	    public double cpuSum = Double.NaN, cpuSumAbs = Double.NaN, cpuMax = Double.NaN;
//	    public double gpuSum = Double.NaN, gpuSumAbs = Double.NaN, gpuMax = Double.NaN;
//
//	    public ParityRecord(String filterName) { this.filterName = filterName; }
//
//	    public String getParity() {
//	        if (Double.isNaN(cpuSum) || Double.isNaN(gpuSum)) return "N/A";
//	        double rel = Math.abs(cpuSum - gpuSum) / (Math.abs(cpuSum) + 1e-9);
//	        double relAbs = Math.abs(cpuSumAbs - gpuSumAbs) / (Math.abs(cpuSumAbs) + 1e-9);
//	        double relMax = Math.abs(cpuMax - gpuMax) / (Math.abs(cpuMax) + 1e-9);
//	        double worst = Math.max(rel, Math.max(relAbs, relMax));
//	        return String.format("%.2e %s", worst, worst < 1e-4 ? "OK" : "*** CHECK ***");
//	    }
//	}
//
//	private static final Map<String, ParityRecord> parity = new LinkedHashMap<>();
//
//	/** Record output signature (sum, sumAbs, max) for a filter+mode. */
//	public static synchronized void recordParity(String filterName, String mode,
//	                                             double sum, double sumAbs, double max) {
//	    ParityRecord r = parity.getOrDefault(filterName, new ParityRecord(filterName));
//	    if ("GPU".equalsIgnoreCase(mode)) { r.gpuSum=sum; r.gpuSumAbs=sumAbs; r.gpuMax=max; }
//	    else                              { r.cpuSum=sum; r.cpuSumAbs=sumAbs; r.cpuMax=max; }
//	    parity.put(filterName, r);
//	}
//
//	public static synchronized List<ParityRecord> getParityResults() {
//	    return new ArrayList<>(parity.values());
//	}

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
