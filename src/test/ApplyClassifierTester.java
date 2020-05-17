package test;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;

import activeSegmentation.IClassifier;
//import activeSegmentation.IProjectManager;
import activeSegmentation.learning.WekaDataSet;
import activeSegmentation.prj.ProjectManager;
import weka.classifiers.functions.SMO;
import weka.core.Instances;



public class ApplyClassifierTester {


	public static void main(String[] args) {

		//TODO replace with a useful test case
		ProjectManager dataManager= new ProjectManager();

		Instances instance=null;
		//dataManager.readDataFromARFF("C:/Users/HP/Desktop/DataImages/aav_samples/aav_samples/test/LIBSVM/LIBSVM/Training1.arff");

		int percent=80;
		instance.randomize(new Random(1));
		int trainSize = (int) Math.round(instance.numInstances() * percent
				/ 100);
		int testSize = instance.numInstances() - trainSize;
		Instances train = new Instances(instance, 0, trainSize);
		Instances test = new Instances(instance, trainSize, testSize);
		IClassifier iclassifier=null; 
				//new WekaClassifier();

		iclassifier.setClassifier(new SMO());
		try {
			iclassifier.buildClassifier(new WekaDataSet(train));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		ForkJoinPool pool = new ForkJoinPool();
		System.out.println("DONE");
		System.out.println(test.size());
		
		
		 double[] classificationResult = new double[test.size()];
		 
		//ApplyTask applyTask= new ApplyTask(test, 0, test.size(), classificationResult, iclassifier);
		//pool.invoke(applyTask);
		/*	for(double result: classificationResult){
			System.out.println(result);
		}
		
		 double[] classificationResult1 =iclassifier.testModel(new WekaDataSet(test));
		 for(int i=0; i< classificationResult.length; i++){
			 if(!(classificationResult1[i]== classificationResult[i])){
				 System.out.println("WRONG");
			 }
		 }*/
	}

}
