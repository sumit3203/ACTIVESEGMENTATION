package activeSegmentation.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SMO;
import activeSegmentation.Common;
import activeSegmentation.IClassifier;
import activeSegmentation.IProjectManager;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeature;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.ILearningManager;
import activeSegmentation.featureSelection.CFS;
import activeSegmentation.featureSelection.PCA;
import activeSegmentation.io.ProjectInfo;

public class ClassifierManager implements ILearningManager {

	private IClassifier currentClassifier= new WekaClassifier(new SMO());
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

	}
	

    @Override
	public void trainClassifier(){

		try {
			currentClassifier.buildClassifier(dataManager.getDataSet());
			System.out.println("Training Results");
			System.out.println(currentClassifier.toString());
			classifierMap.put(currentClassifier.getClass().getCanonicalName(), currentClassifier);
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
		if (classifier instanceof AbstractClassifier) {
			currentClassifier = new WekaClassifier((AbstractClassifier)classifier);		 		
		}
	}

    @Override
	public double[] applyClassifier(IDataSet dataSet){
		System.out.println("Testing Results");
			System.out.println("INSTANCE SIZE"+ dataSet.getNumInstances());
			System.out.println("WORK LOAD : "+ Common.WORKLOAD);
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

}