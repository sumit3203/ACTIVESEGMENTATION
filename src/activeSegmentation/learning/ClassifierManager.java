package activeSegmentation.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

//import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
//import weka.core.Instances;
import activeSegmentation.ASCommon;
import activeSegmentation.IClassifier;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.InstanceUtil;
//import ij.IJ;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;



public class ClassifierManager implements ASCommon {

	private IClassifier currentClassifier= new WekaClassifier(new RandomForest());
	Map<String,IClassifier> classifierMap= new HashMap< >();
	private ProjectManager dataManager;
	private ProjectInfo metaInfo;
	private List<String> learningList;
	//private String selectedType=ASCommon.PASSIVELEARNING;
	private IDataSet dataset;
	private ForkJoinPool pool; 
	private Map<String,IFeatureSelection> featureMap;
	
	
	/**
	 * 
	 * @param dataManager
	 */
	public ClassifierManager(ProjectManager dataManager){
		learningList= new ArrayList<>();
		featureMap=new HashMap<>();
		learningList.add(ASCommon.ACTIVELEARNING);
		learningList.add(ASCommon.PASSIVELEARNING);
		featureMap.put("CFS", new CFS());
		featureMap.put("PCA", new PCA());
		this.dataManager= dataManager;
		pool=  new ForkJoinPool();
	}
	
	/**
	 * 
	 */
	public void trainClassifier(){
    	metaInfo= dataManager.getMetaInfo();
    	System.out.println("Classifier Manager: in training");
    	File folder = new File(metaInfo.getProjectDirectory().get(ASCommon.K_LEARNINGDIR));
    	
		//System.out.println("ground truth "+metaInfo.getProjectDirectory().get(ASCommon.K_LEARNINGDIR)+metaInfo.getGroundtruth());
		try {
			//System.out.println("Classifier Manager: in training");
	
			String filename=folder.getCanonicalPath()+fs+metaInfo.getGroundtruth();
			//IJ.log(filename);
			if (metaInfo.getGroundtruth()!=null && !metaInfo.getGroundtruth().isEmpty()){
				System.out.println("Classifier Manager: reading ground truth "+filename);
				dataset=InstanceUtil.readDataFromARFF(filename);
				//System.out.println("ClassifierManager: in learning");
			}
			if(dataset!=null) {
				IDataSet data = dataManager.getDataSet();
				dataset.getDataset().addAll(data.getDataset());
			} else {
				dataset=dataManager.getDataSet();
			}
			//System.out.println("writing file");
	
			currentClassifier.buildClassifier(dataset);
			//
			System.out.println("Classifier summary");
			
			String outputstr=currentClassifier.toString();
			System.out.println(outputstr);
			
			// print summary here

			outputstr+= currentClassifier.evaluateModel(dataset);
			 
			
			InstanceUtil.writeDataToTXT(outputstr, metaInfo);
			
			
			// to avoid data creep
			dataset.delete();
		
		} catch (Exception e) {
		
			e.printStackTrace();
		}
	}
	
	
	public void saveLearningMetaData(){	
		metaInfo= dataManager.getMetaInfo();
		if(dataset!=null){
			InstanceUtil.writeDataToARFF(dataset.getDataset(), metaInfo);
		}

		dataManager.writeMetaInfo(metaInfo);		
	}

	// where do we call this method?
	/*
	public void loadLearningMetaData() {
		if(metaInfo.getLearning()!=null){
			dataset= InstanceUtil.readDataFromARFF(metaInfo.getLearning().get(ASCommon.ARFF));
			selectedType=metaInfo.getLearning().get(ASCommon.LEARNINGTYPE);
		}
	}
	*/

	public void setClassifier(Object classifier) {
		currentClassifier = (WekaClassifier)classifier;		 	
		//System.out.println(currentClassifier.toString());
	}

	public double[] applyClassifier(IDataSet dataSet){
			final int ni=dataSet.getNumInstances();
			double[] classificationResult = new double[ni];	
			try {
				ApplyTask applyTask= new ApplyTask(dataSet, 0, ni, classificationResult, currentClassifier);
				pool.invoke(applyTask);
			} catch (@SuppressWarnings("unused") Exception ex) {
				System.out.println("Exception in applyClassifier ");
			}
		return classificationResult;
	}

	public Set<String> getFeatureSelList() {
		return featureMap.keySet();
	}


	public double predict(Instance instance) {
		try {
			return currentClassifier.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return PREDERR;
	}

	public static final int PREDERR=-1;

	public Object getClassifier() {
		return this.currentClassifier.getClassifier();
	}

}
