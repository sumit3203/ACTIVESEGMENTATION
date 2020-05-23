package test;

import java.util.Arrays;

//import activeSegmentation.IProjectManager;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.CFS;
import activeSegmentation.learning.GainRatio;
import activeSegmentation.learning.PCA;
import activeSegmentation.prj.ProjectManager;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;

public class TestFeatureSelection {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ProjectManager dataManager= new ProjectManager();
		IDataSet trainingData=dataManager.readDataFromARFF("D:/DATASET/GFAPED2/GFAPED1/TrainingData1_Randomise.arff");
		IFeatureSelection featureSelection= new GainRatio();
		IDataSet trainingInstance= featureSelection.selectFeatures(trainingData);
		System.out.println(trainingInstance.getDataset().toSummaryString());
		IDataSet testDataset=dataManager.readDataFromARFF("D:/DATASET/GFAPED2/GFAPED1/Testdata_Randomise.arff");
		IDataSet testData=  featureSelection.applyOnTestData(testDataset);
		System.out.println(testData.getDataset().toSummaryString());
		//SMO smo = new SMO();
		NaiveBayes smo= new NaiveBayes();
		try {
			smo.buildClassifier(trainingInstance.getDataset());
			System.out.println(smo.toString());
			//Evaluation eval = new Evaluation(trainingInstance.getDataset());
			// eval.evaluateModel(smo, testData.getDataset());
			// System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			for(Instance instance: testData.getDataset()){
				System.out.println(Arrays.toString(smo.distributionForInstance(instance)));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
