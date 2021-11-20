package activeSegmentation.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import activeSegmentation.ASCommon;
import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;

public class ApplyClassifier {

	private IClassifier iclassifier=null;
	private List<IDataSet> testDataSet=null;
	private ForkJoinPool pool = new ForkJoinPool();; 

	/**
	 * 
	 * @param iclassifier
	 * @param testDataSet
	 */
	public ApplyClassifier(IClassifier iclassifier, List<IDataSet> testDataSet) {
		this.iclassifier = iclassifier;
		this.testDataSet = testDataSet;
	}

	/**
	 * Applies the classifier to the data set
	 * @return
	 */
	public List<double[]> applyClassifier(){
		List<double[]> classificationList= new ArrayList<>();
		for (IDataSet dataSet: testDataSet){
			double[] classificationResult = new double[dataSet.getNumInstances()];
			ApplyTask applyTask= new ApplyTask(dataSet, 0, dataSet.getNumInstances(), classificationResult, iclassifier);
			pool.invoke(applyTask);
			classificationList.add(classificationResult);
		}
		return classificationList;
	}
	/*
		public void generateProbabilityMask(){
	
		}
	*/
}
