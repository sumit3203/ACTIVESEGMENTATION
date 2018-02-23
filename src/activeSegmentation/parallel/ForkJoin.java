package activeSegmentation.parallel;

/*
 * basic interface for divide and conquer algorithms
 * @author Dimiter Prodanov
 */
public interface ForkJoin {

	/*
	 *  forking a task
	 */
	public abstract void fork(int id);

	/*
	 * joining all tasks
	 */
	public abstract void join();

}