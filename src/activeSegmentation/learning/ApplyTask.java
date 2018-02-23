package activeSegmentation.learning;

import java.util.concurrent.RecursiveAction;

import activeSegmentation.Common;
import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
import weka.core.Instances;

public class ApplyTask extends RecursiveAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IDataSet dataSet;
	private double[] classificationResult;
	private IClassifier iClassifier;
	private Integer mStart;
	private int mLength;

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
		// TODO Auto-generated method stub
		if (mLength < Common.WORKLOAD) {
			classifyPixels();
			return;
		}
		//System.out.println("mLength"+mLength);

		Integer split = mLength / 2;

		invokeAll(new ApplyTask(dataSet, mStart, split, classificationResult,iClassifier),
				new ApplyTask(dataSet, mStart + split, mLength - split, 
						classificationResult,iClassifier));

	}

	private void classifyPixels(){
		IClassifier classifierCopy=null;
		try {
			classifierCopy = (IClassifier) (iClassifier.makeCopy());
		} catch (Exception e) {

			e.printStackTrace();
		}

		Instances testInstances= new Instances(dataSet.getDataset(), mStart, mLength);
		for (int index = 0; index < testInstances.size(); index++)
		{
			try {
				
				classificationResult[mStart+index]=classifierCopy.
				classifyInstance(testInstances.get(index));
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

}
