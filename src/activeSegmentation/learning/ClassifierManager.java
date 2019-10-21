package activeSegmentation.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import activeSegmentation.Common;
import activeSegmentation.IClassifier;
import activeSegmentation.IProjectManager;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.ILearningManager;
import activeSegmentation.featureSelection.CFS;
import activeSegmentation.featureSelection.PCA;
import activeSegmentation.io.ProjectInfo;
import bsh.This;

public class ClassifierManager implements ILearningManager {

	private IClassifier currentClassifier= new WekaClassifier(new J48());
	Map<String,IClassifier> classifierMap= new HashMap<String, IClassifier>();
	private IProjectManager dataManager;
	private ProjectInfo metaInfo;
	private List<String> learningList;
	private String selectedType=Common.PASSIVELEARNING;
	private IDataSet dataset;
	private ForkJoinPool pool; 
	private Map<String,IFeatureSelection> featureMap;
	
	public ClassifierManager(IProjectManager dataManager){
		learningList= new ArrayList<String>();
		featureMap=new HashMap<String,IFeatureSelection>();
		learningList.add(Common.ACTIVELEARNING);
		learningList.add(Common.PASSIVELEARNING);
		featureMap.put("CFS", new CFS());
		featureMap.put("PCA", new PCA());
		this.dataManager= dataManager;
		pool=  new ForkJoinPool();
		//dataset= dataManager.readDataFromARFF("C:\\Users\\sumit\\Documents\\demo\\test-eigen\\Training\\learning\\training.arff");

	}
	

    @Override
	public void trainClassifier(){
    	metaInfo= dataManager.getMetaInfo();
    	System.out.println("in training");
    	File folder = new File(this.metaInfo.getProjectDirectory().get(Common.LEARNINGDIR));
    	
		System.out.println(this.metaInfo.getProjectDirectory().get(Common.LEARNINGDIR)+this.metaInfo.getGroundtruth());
		try {
			System.out.println("in training");
		//	System.out.println(folder.getCanonicalPath()+this.metaInfo.getGroundtruth());
			String filename=folder.getCanonicalPath()+"\\"+this.metaInfo.getGroundtruth();
			if(this.metaInfo.getGroundtruth()!=null && !this.metaInfo.getGroundtruth().isEmpty())
			{
				System.out.println(filename);
				dataset=dataManager.readDataFromARFF(filename);
				System.out.println("in learning");
			}
			if(dataset!=null) {
				dataset.getDataset().addAll(dataManager.getDataSet().getDataset());
			}
			else {
				dataset=dataManager.getDataSet();
			}
			//System.out.println("writing file");
			//dataManager.writeDataToARFF(dataset.getDataset(), "\\test-eigen\\Training\\learning\\training1.arff");

			currentClassifier.buildClassifier(dataset);
			//
			//System.out.println("Training Results");
			System.out.println(currentClassifier.toString());
			//classifierMap.put(currentClassifier.getClass().getCanonicalName(), currentClassifier);
		} catch (Exception e) {
		
			e.printStackTrace();
		}
	}

	@Override
	public void saveLearningMetaData(){	
		metaInfo= dataManager.getMetaInfo();
		Map<String,String> learningMap = new HashMap<String, String>();
		if(dataset!=null){
			learningMap.put(Common.ARFF, Common.ARFFFILENAME);
			dataManager.writeDataToARFF(dataset.getDataset(), Common.ARFFFILENAME);		
		}
		//learningMap.put(Common.CLASSIFIER, Common.CLASSIFIERNAME);  
		learningMap.put(Common.LEARNINGTYPE, selectedType);
		metaInfo.setLearning(learningMap);
		dataManager.writeMetaInfo(metaInfo);		
	}

	@Override
	public void loadLearningMetaData() {
		// TODO Auto-generated method stub
		if(metaInfo.getLearning()!=null){
			dataset= dataManager.readDataFromARFF(metaInfo.getLearning().get(Common.ARFF));
			selectedType=metaInfo.getLearning().get(Common.LEARNINGTYPE);
		}
	}

	@Override
	public void setClassifier(Object classifier) {
		System.out.println(classifier.toString());
			currentClassifier = (WekaClassifier)classifier;		 	
			System.out.println(currentClassifier.toString());
		
	}

    @Override
	public double[] applyClassifier(IDataSet dataSet){
		//System.out.println("Testing Results");
		//	System.out.println("INSTANCE SIZE"+ dataSet.getNumInstances());
		//	System.out.println("WORK LOAD : "+ Common.WORKLOAD);
			double[] classificationResult = new double[dataSet.getNumInstances()];		
			ApplyTask applyTask= new ApplyTask(dataSet, 0, dataSet.getNumInstances(), 
					classificationResult, currentClassifier);
					pool.invoke(applyTask);
							
			
		return classificationResult;
	}


	@Override
	public Set<String> getFeatureSelList() {
		
		return featureMap.keySet();
	}


	@Override
	public double predict(Instance instance) {
		// TODO Auto-generated method stub
		try {
			return currentClassifier.classifyInstance(instance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}


	@Override
	public Object getClassifier() {
		// TODO Auto-generated method stub
		return this.currentClassifier.getClassifier();
	}

}