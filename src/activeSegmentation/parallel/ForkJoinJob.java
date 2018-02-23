package activeSegmentation.parallel;


import java.util.concurrent.Callable;

/*
 * Interface for divide and conquer algorithms extending Callable<T>
 * @author Dimiter Prodanov
 */
public interface ForkJoinJob<T> extends Callable<T>, ForkJoin {

	 
}
