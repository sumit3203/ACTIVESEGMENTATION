package activeSegmentation.parallel;

import java.util.concurrent.atomic.AtomicInteger;

public class ParallelFramework {

	public static final int n_cpus = Runtime.getRuntime().availableProcessors();
	public static boolean debug = false;
	protected long time = -1;
	protected AtomicInteger icounter=new AtomicInteger(0);
	
	public ParallelFramework() {
		super();
	}

	public long time() {
		return time;
	}

	public boolean isCalled() {
		return time>-1;
	}

	/**
	 * @return the counter
	 */
	public AtomicInteger getCounter() {
		return icounter;
	}

}