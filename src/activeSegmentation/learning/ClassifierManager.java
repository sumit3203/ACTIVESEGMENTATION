package activeSegmentation.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import weka.classifiers.AbstractClassifier;
//import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
//import weka.core.Instances;
import activeSegmentation.ASCommon;
import activeSegmentation.IClassifier;
import activeSegmentation.prj.LearningInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.InstanceUtil;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaClassifier;



public class ClassifierManager implements ASCommon {

	private IClassifier currentClassifier= new WekaClassifier(new RandomForest());

	private ProjectManager projectMan;
	private ProjectInfo projectInfo;
	private List<String> learningList;

	private IDataSet dataset;
	private ForkJoinPool pool=  new ForkJoinPool();
	private ArrayList<IFeatureSelection> featureMap=new ArrayList<>();
	
	public static final int PREDERR=-1;
	
	/**
	 * 
	 * @param dataManager
	 */
	public ClassifierManager(ProjectManager dataManager){
		learningList= new ArrayList<>();
		learningList.add(ASCommon.ACTIVELEARNING);
		learningList.add(ASCommon.PASSIVELEARNING);
	 	
		featureMap.add(new CFS());
		featureMap.add(new PCA());
		projectMan = dataManager;
		projectInfo= dataManager.getMetaInfo();
	}
	
	/**
	 * 
	 */
	public void trainClassifier(){
    	projectInfo= projectMan.getMetaInfo();
    	System.out.println("Classifier Manager: in training");
    	File folder = new File(projectInfo.getProjectDirectory().get(ASCommon.K_LEARNINGDIR));
    	
		//System.out.println("ground truth "+metaInfo.getProjectDirectory().get(ASCommon.K_LEARNINGDIR)+metaInfo.getGroundtruth());
		try {
			//System.out.println("Classifier Manager: in training");
			// do we need this?
			String filename=folder.getCanonicalPath()+fs+projectInfo.getGroundtruth();
			//IJ.log(filename);
			if (projectInfo.getGroundtruth()!=null && !projectInfo.getGroundtruth().isEmpty()){
				System.out.println("Classifier Manager: reading ground truth "+filename);
				dataset=InstanceUtil.readDataFromARFF(filename);
				//System.out.println("ClassifiegrManager: in learning");
			}
			if(dataset!=null) {
				IDataSet data = projectMan.getDataSet();
				dataset.getDataset().addAll(data.getDataset());
			} else {
				dataset=projectMan.getDataSet();
			}
		
			//TODO select features here;
			LearningInfo li= projectInfo.getLearning();
			String cname= li.getLearningOption();
			
			if (cname!="") 
				System.out.println(cname);
			
			
			currentClassifier.buildClassifier(dataset);
			
			if(dataset!=null)
				InstanceUtil.writeDataToARFF(dataset.getDataset(), projectInfo);
			
			if (currentClassifier!=null)
				InstanceUtil.writeClassifier( (AbstractClassifier) currentClassifier.getClassifier(), projectInfo);

			projectMan.writeMetaInfo(projectInfo);		
			
			// move to evaluation;
			System.out.println("Classifier summary");
			
			String outputstr=currentClassifier.toString();
			// print summary here
			System.out.println(outputstr);
			
			outputstr+= currentClassifier.evaluateModel(dataset);
			 
			//Wring output-> move to evaluation;
			InstanceUtil.writeDataToTXT(outputstr, projectInfo);
			
			// to avoid data creep
			dataset.delete();
		
		} catch (Exception e) {		
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void saveLearningMetaData(){	
		projectInfo= projectMan.getMetaInfo();
		projectMan.writeMetaInfo(projectInfo);		
	}

	/**
	 * 
	 */
	public LearningInfo getLearningMetaData() {
		return projectInfo.getLearning();
	}

	/**
	 * 
	 * @param classifier
	 */
	public void setClassifier(Object classifier) {
		currentClassifier = (WekaClassifier)classifier;		 	
		//System.out.println(currentClassifier.toString());
	}

	/**
	 * 
	 * @param dataSet
	 * @return
	 */
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

	/**
	 * 
	 * @return
	 */
	public ArrayList<IFeatureSelection> getFeatureSelList() {
		return featureMap;
	}

	/**
	 * 
	 * @param instance
	 * @return
	 */
	public double predict(Instance instance) {
		try {
			return currentClassifier.classifyInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return PREDERR;
	}


	/**
	 * 
	 * @return
	 */
	public Object getClassifier() {
		return currentClassifier.getClassifier();
	}
	
	public double[] getDistribution(Instance instance) {
		try {
			return currentClassifier.distributionForInstance(instance);
		} catch(Exception e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
