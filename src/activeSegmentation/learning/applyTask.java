package activeSegmentation.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import weka.core.Instances;
import weka.core.SerializedObject;

public class applyTask extends RecursiveAction{

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
	public applyTask(IDataSet dataSet,Integer mStart,int length, double[] classificationResult, IClassifier classifier) {
		System.out.println("ApplyTask: fdata null");
		this.dataSet = dataSet;
 
		this.classificationResult= classificationResult;
		this.iClassifier=classifier;
		this.mStart= mStart;
		this.mLength= length;

	}


	/**
	 * 
	 * @param dataSet
	 * @param mStart
	 * @param length
	 * @param classificationResult
	 * @param classifier
	 */
	public applyTask(IDataSet dataSet,Integer mStart,int length, double[] classificationResult, 
			IClassifier classifier, IFeatureSelection filter) {
		
		if (filter!=null) {
			IDataSet fdata = filter.selectFeatures(dataSet);
			this.dataSet = fdata;
		} else {
			System.out.println("ApplyTask: fdata null");
			throw new RuntimeException();
		}
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
			if (filter!=null)
				invokeAll(createSubtasks(filter));
			else 
				invokeAll(createSubtasks());
		}
	}
	 
	private List<applyTask> createSubtasks(IFeatureSelection filter) {
        List<applyTask> subtasks = new ArrayList<>();
        final int split = mLength / 2;
        //divide and conquer tree recursion
        applyTask task1 = new applyTask(dataSet, mStart,         split,           classificationResult, iClassifier, filter);
        applyTask task2 = new applyTask(dataSet, mStart + split, mLength - split, classificationResult, iClassifier, filter);

        subtasks.add(task1);
        subtasks.add(task2);

        return subtasks;
    }
	
	private List<applyTask> createSubtasks() {
        List<applyTask> subtasks = new ArrayList<>();
        final int split = mLength / 2;
        //divide and conquer tree recursion
        applyTask task1 = new applyTask(dataSet, mStart,         split,           classificationResult, iClassifier);
        applyTask task2 = new applyTask(dataSet, mStart + split, mLength - split, classificationResult, iClassifier);

        subtasks.add(task1);
        subtasks.add(task2);

        return subtasks;
    }
	 
	private void classifyPixels(){
		try {
			IClassifier classifierCopy = (IClassifier) (iClassifier.makeCopy()); 
			//Instances testInstances= new Instances(dataSet.getDataset(), mStart, mLength);
			Instances testInstances= new Instances(dataSet.getDataset(), mStart, mLength);
			
			for (int index = 0; index < testInstances.size(); index++){				
					classificationResult[mStart+index]=classifierCopy.
					classifyInstance(testInstances.get(index));
			}
		} catch (Exception e) {	
				e.printStackTrace();
				throw new RuntimeException();
		}
	}

}
