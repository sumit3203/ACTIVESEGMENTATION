package activeSegmentation.evaluation;

import ijaux.scale.Pair;

public class ROC_CURVE_TEST {

	public static void main(String[] args)
	  {
	    double[] labels = { 0.0D, 0.0D, 1.0D, 1.0D };
	    double[] scores = { 0.1D, 0.4D, 0.35D, 0.8D };
	    
	    Pair<double[], int[]> sortedArray = NPArray.sort(scores, false);
	    double[] sortedLabels = NPArray.sortByIndex(labels, (int[])sortedArray.second);
	    int[] distinctIndices = NPArray.r_(NPArray.where(NPArray.diff((double[])sortedArray.first), 0.0D), new int[] {
	      labels.length - 1 });
	    
	    double[] tps = NPArray.sliceByIndices(NPArray.cumSum(sortedLabels), distinctIndices);
	    double[] fps = NPArray.diff(NPArray.intTodouble(NPArray.addScaler(distinctIndices, 1)), tps);
	    double[] tpsN = NPArray.normalize(tps, tps[(tps.length - 1)]);
	    double[] fpsN = NPArray.normalize(fps, fps[(fps.length - 1)]);
	    for (int i = 0; i < fpsN.length; i++) {
	      System.out.println(fpsN[i] + "-- " + tpsN[i]);
	    }
	  }
}
