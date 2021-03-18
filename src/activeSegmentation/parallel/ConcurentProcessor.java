package activeSegmentation.parallel;


import java.util.*;
import java.util.concurrent.*;

/*
 *  possibly to put in PixLib
 */

/*
 * basic implementation
 */

public class ConcurentProcessor<T> extends ParallelFramework implements Callable<T> {

	private ThreadFactory factory =	Executors.defaultThreadFactory();

	private ThreadPoolExecutor executor;
	
	private ArrayList<ForkJoinJob<T>> jobs=new ArrayList<>();;
	
		
	/*
	 * default constructor
	 */
	public ConcurentProcessor() {
		executor =(ThreadPoolExecutor) Executors.newFixedThreadPool(n_cpus, factory);	
		//if(debug)
			System.out.println("starting a ConcurentProcessor " + n_cpus);
	}
	
	/*
	 * scalable constructor
	 */
	public ConcurentProcessor(float factor) {
		if (factor >4)
			factor=4;
		int poolsize= (int)(factor* n_cpus);				
		executor =(ThreadPoolExecutor) Executors.newFixedThreadPool(poolsize, factory);	
		if(debug)
			System.out.println("starting a ConcurentProcessor " + poolsize);
	}
	
	@Override
	public T call() {
		time=-System.currentTimeMillis();
		int sz=jobs.size();
		if (sz>0) {
			try {					
				exec((Executor)executor,jobs);
			} catch (InterruptedException e) {
				icounter.incrementAndGet();
				e.printStackTrace();
				//time=0L;
			}
			executor.shutdown();
			for (ForkJoinJob<?> c: jobs) {
				c.join();
			}
			time+=System.currentTimeMillis();
		
		}		
		return null;
	}
	
	public void reset() {
		time=-1;
		jobs.clear();
	}

	/*
	 * adds a job to the list and forks
	 */
	public void addJob(ForkJoinJob<T> task) {
		if (jobs.add(task)) {
				int _id=jobs.indexOf(task);
				task.fork(_id);
		}
	}
	
	/*
	 * adds an ArrayList of jobs and forks all
	 */
	public void addJobList(ArrayList<ForkJoinJob<T>> tjobs) {
		reset();
		jobs=tjobs;
		for (ForkJoinJob<T> task: jobs) {
			int _id=jobs.indexOf(task);
			task.fork(_id);
		}
	}
	
	/*
	 * Executes a job list
	 */
	public void exec(Executor e,  ArrayList<ForkJoinJob<T>> jobs)
			throws InterruptedException {
		ExecutorCompletionService <T> ecs = new ExecutorCompletionService<>(e);
		for (Callable<T> s : jobs) {
			ecs.submit(s);
		}
		final int n = jobs.size();
		int cnt=0;
		if (debug)
			System.out.println("retriving tasks ... " +n);

		while (cnt<n ) {		        	
			Future<T> ft=ecs.take();
			if (ft.isDone()) {
				cnt++;
			}

		}

	}
}
