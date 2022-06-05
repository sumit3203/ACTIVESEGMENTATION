package activeSegmentation.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;


import weka.classifiers.AbstractClassifier;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;

import activeSegmentation.ASCommon;

import activeSegmentation.IClassifier;
import activeSegmentation.prj.LearningInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.InstanceUtil;
import ij.IJ;
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
	private HashMap<String,IFeatureSelection> featureMap=new HashMap<>();
	
	public static final int PREDERR=-1;
	
	/**
	 * 
	 * @param dataManager
	 */
	public ClassifierManager(ProjectManager dataManager){
		learningList= new ArrayList<>();
		learningList.add(ASCommon.ACTIVELEARNING);
		learningList.add(ASCommon.PASSIVELEARNING);
	 	
		featureMap.put("activeSegmentation.learning.CFS",new CFS());
		featureMap.put("activeSegmentation.learning.PCA",new PCA());
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
    	
		try {
			//System.out.println("Classifier Manager: in training");
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
		
		
			LearningInfo li= projectInfo.getLearning();
			String cname= li.getLearningOption();
			//System.out.println("cname "+ cname);
			if (cname!="")  {			
			 	IFeatureSelection cclass =featureMap.get(cname);
			 	if (dataset==null) {
			 		IJ.log("Classifier Manager: error in training:"+ cclass.getName() +" is null");
			 	}
				currentClassifier.buildClassifier(dataset, cclass);							
			} else
				currentClassifier.buildClassifier(dataset);
			
			//currentClassifier.buildClassifier(dataset);
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
			 
			//Write output-> move to evaluation;
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
			LearningInfo li= projectInfo.getLearning();
			String cname= li.getLearningOption();
			System.out.print("learning option "+ cname);
			IDataSet fdata=null;
			if (cname!="")  {
				IFeatureSelection cclass =featureMap.get(cname);
				System.out.print("Classifier Manager: selecting feature " +cclass. getName()+ " "+cname);
				fdata=cclass.applyOnTestData(dataSet);
			}
			
			double[] classificationResult = new double[ni];	
			if (fdata!=null) {
				try {
					ApplyTask applyTask= new ApplyTask(fdata, 0, ni, classificationResult, currentClassifier);
					//System.out.println("cname "+ cname);			 	
					IFeatureSelection sel =featureMap.get(cname);			 	
					applyTask.setFilter(sel);
					pool.invoke(applyTask);
				} catch ( Exception ex) {
					System.out.println("Exception in applyClassifier ");
					ex.printStackTrace();
				}
			} else {
				System.out.println("Classifier Manager: applyClassifier: fdata set is null"); 
				try {
					ApplyTask applyTask= new ApplyTask(dataSet, 0, ni, classificationResult, currentClassifier);
					pool.invoke(applyTask);
				} catch ( Exception ex) {
					System.out.println("Exception in applyClassifier ");
					ex.printStackTrace();
				}
			}
		return classificationResult;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getFeatureSelSet() {
			
		return featureMap.keySet();
	}


	/**
	 * 
	 * @return
	 */
	public HashMap<String,IFeatureSelection> getFeatureSelMap() {
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

}
