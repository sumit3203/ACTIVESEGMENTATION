package activeSegmentation.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import weka.core.Instances;
import weka.core.SerializedObject;

public class ApplyTask extends RecursiveAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IDataSet dataSet;
	private double[] classificationResult;
	private IClassifier iClassifier;

	private int mStart=0;
	private int mLength=512;
	
	private boolean debug=false;

	private IFeatureSelection filter;
	public void setFilter(IFeatureSelection selection) {
		filter=selection;
	}

	/**
	 * 
	 * @param dataSet
	 * @param mStart
	 * @param length
	 * @param classificationResult
	 * @param classifier
	 */
	public ApplyTask(IDataSet dataSet,Integer mStart,int length, double[] classificationResult, 
			IClassifier classifier) {
		this.dataSet = dataSet;
		this.classificationResult= classificationResult;
		this.iClassifier=classifier;
		this.mStart= mStart;
		this.mLength= length;

	}

	@Override
	protected void compute() {
		if (mLength < 1024) {		
			classifyPixels();		 
		} else {
			if (debug)
				System.out.println("ApplyTask: splitting workLoad: " + mLength);	
			invokeAll(createSubtasks());
		}
	}
	 
	private List<ApplyTask> createSubtasks() {
        List<ApplyTask> subtasks = new ArrayList<>();
        final int split = mLength / 2;
        //divide and conquer tree recursion
        ApplyTask task1 = new ApplyTask(dataSet, mStart,         split,           classificationResult, iClassifier);
        ApplyTask task2 = new ApplyTask(dataSet, mStart + split, mLength - split, classificationResult, iClassifier);

        subtasks.add(task1);
        subtasks.add(task2);

        return subtasks;
    }
	 
	private void classifyPixels(){
		//try {
			IClassifier classifierCopy = (IClassifier) (iClassifier.makeCopy()); 
			Instances testInstances= new Instances(dataSet.getDataset(), mStart, mLength);
			// 
			// new SerializedObject(this).getObject();
			//applyOnTestData (testInstances);
			for (int index = 0; index < testInstances.size(); index++){				
					classificationResult[mStart+index]=classifierCopy.
					classifyInstance(testInstances.get(index));
			}
		//} catch (Exception e) {	
		//		e.printStackTrace();
		//}
	}

}
