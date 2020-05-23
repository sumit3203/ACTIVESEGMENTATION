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
import activeSegmentation.ASCommon;
import activeSegmentation.IClassifier;
//import activeSegmentation.IProjectManager;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
//import activeSegmentation.LearningManager;

///public class ClassifierManager implements ILearningManager {
public class ClassifierManager  {

	private IClassifier currentClassifier= new WekaClassifier(new RandomForest());
	Map<String,IClassifier> classifierMap= new HashMap<String, IClassifier>();
	private ProjectManager dataManager;
	private ProjectInfo metaInfo;
	private List<String> learningList;
	private String selectedType=ASCommon.PASSIVELEARNING;
	private IDataSet dataset;
	private ForkJoinPool pool; 
	private Map<String,IFeatureSelection> featureMap;
	
	public ClassifierManager(ProjectManager dataManager){
		learningList= new ArrayList<String>();
		featureMap=new HashMap<String,IFeatureSelection>();
		learningList.add(ASCommon.ACTIVELEARNING);
		learningList.add(ASCommon.PASSIVELEARNING);
		featureMap.put("CFS", new CFS());
		featureMap.put("PCA", new PCA());
		this.dataManager= dataManager;
		pool=  new ForkJoinPool();
		//dataset= dataManager.readDataFromARFF("C:\\Users\\sumit\\Documents\\demo\\test-eigen\\Training\\learning\\training.arff");

	}
	

   // @Override
	public void trainClassifier(){
    	metaInfo= dataManager.getMetaInfo();
    	System.out.println("in training");
    	File folder = new File(this.metaInfo.getProjectDirectory().get(ASCommon.LEARNINGDIR));
    	
		System.out.println(this.metaInfo.getProjectDirectory().get(ASCommon.LEARNINGDIR)+this.metaInfo.getGroundtruth());
		try {
			System.out.println("ClassifierManager: in training");
		//	System.out.println(folder.getCanonicalPath()+this.metaInfo.getGroundtruth());
			String filename=folder.getCanonicalPath()+"\\"+this.metaInfo.getGroundtruth();
			if(this.metaInfo.getGroundtruth()!=null && !this.metaInfo.getGroundtruth().isEmpty())
			{
				System.out.println(filename);
				dataset=dataManager.readDataFromARFF(filename);
				System.out.println("ClassifierManager: in learning");
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

	//@Override
	public void saveLearningMetaData(){	
		metaInfo= dataManager.getMetaInfo();
		Map<String,String> learningMap = new HashMap<String, String>();
		if(dataset!=null){
			learningMap.put(ASCommon.ARFF, ASCommon.ARFFFILENAME);
			dataManager.writeDataToARFF(dataset.getDataset(), ASCommon.ARFFFILENAME);		
		}
		//learningMap.put(Common.CLASSIFIER, Common.CLASSIFIERNAME);  
		learningMap.put(ASCommon.LEARNINGTYPE, selectedType);
		metaInfo.setLearning(learningMap);
		dataManager.writeMetaInfo(metaInfo);		
	}

	//@Override
	public void loadLearningMetaData() {
		if(metaInfo.getLearning()!=null){
			dataset= dataManager.readDataFromARFF(metaInfo.getLearning().get(ASCommon.ARFF));
			selectedType=metaInfo.getLearning().get(ASCommon.LEARNINGTYPE);
		}
	}

	//@Override
	public void setClassifier(Object classifier) {
		//System.out.println(classifier.toString());
			currentClassifier = (WekaClassifier)classifier;		 	
			System.out.println(currentClassifier.toString());
		
	}

   // @Override
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


//	@Override
	public Set<String> getFeatureSelList() {
		
		return featureMap.keySet();
	}


//	@Override
	public double predict(Instance instance) {
		try {
			return currentClassifier.classifyInstance(instance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}


//	@Override
	public Object getClassifier() {
		return this.currentClassifier.getClassifier();
	}

}
