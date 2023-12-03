package activeSegmentation.learning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import weka.classifiers.AbstractClassifier;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;

import activeSegmentation.ASCommon;
import activeSegmentation.FilterType;
import activeSegmentation.IAnnotated;
import activeSegmentation.IClassLoader;
import activeSegmentation.IClassifier;
import activeSegmentation.prj.LearningInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.InstanceUtil;
import ij.IJ;
import ijaux.datatype.Pair;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.IFilter;
import activeSegmentation.ProjectType;
import activeSegmentation.filter.FilterManager;
import activeSegmentation.learning.weka.WekaClassifier;



public class ClassifierManager extends URLClassLoader implements ASCommon, IClassLoader<IFeatureSelection> {

	private IClassifier currentClassifier= new WekaClassifier(new RandomForest());

	private ProjectManager projectMan;
	private ProjectInfo projectInfo;
	private List<String> learningList;

	private IDataSet dataset;
	private ForkJoinPool pool=  new ForkJoinPool();
	private  TreeMap<String, IFeatureSelection>  featureMap=new TreeMap<>();
	
	public static final int PREDERR=-1;
	
	/**
	 * 
	 * @param dataManager
	 */
	public ClassifierManager(ProjectManager dataManager){
		super(new URL[0], IJ.class.getClassLoader());
		learningList= new ArrayList<>();
		learningList.add(ASCommon.ACTIVELEARNING);
		learningList.add(ASCommon.PASSIVELEARNING);
	 	
		projectMan = dataManager;
		projectInfo= dataManager.getMetaInfo();
		
		System.out.println("ClassifierManager:loading features");
		//// implement automatic loading based on IFeatureSelection
		/*
		featureMap.put("activeSegmentation.learning.ID",new ID());
		featureMap.put("activeSegmentation.learning.CFS",new CFS());
		featureMap.put("activeSegmentation.learning.PCA",new PCA());
		featureMap.put("activeSegmentation.learning.InfoGain",new InfoGain());
		featureMap.put("activeSegmentation.learning.GainRatio",new GainRatio());
		*/
		
		try {
			List<String> jars=projectInfo.getPluginPath();
			System.out.println("AS plugin path: "+jars);
			if (jars!=null)
				loadSelectionFilters(jars);
			System.out.println("Selection Filters loaded: "+jars);
			IJ.log("Selection Filters loaded");
		} catch ( Exception e) {
			e.printStackTrace();
			IJ.log("Selection Filters NOT loaded. Check pluginPath variable");
		}
		/* */
		System.out.println("ClassifierManager: end") ;
	}
	
	private void addJar(File f) {
		System.out.println("addJar");
		if (f.getName().endsWith(".jar")) {
			try {
				addURL(f.toURI().toURL());
			} catch (MalformedURLException e) {
				System.out.println("PluginClassLoader: "+e);
			}
		}
	}
	
 
	
	public  void loadSelectionFilters(List<String> plugins) throws 
	InstantiationException, IllegalAccessException, IOException {

		System.out.println("loadSelectionFilters");
		List<String> classes=new ArrayList<>();
		String cp=System.getProperty("java.class.path");
		System.out.println ("classpath "+ cp);
		for(String plugin: plugins){
			System.out.println("plugin "+plugin);
			File g = new File(plugin);
			if(plugin.endsWith(".jar") && g.isFile())	{ 
				classes.addAll(installJarPlugins(plugin));
				cp+=";" + plugin;
				System.setProperty("java.class.path", cp);
				
				addJar(g);
			}
		}
		
		
		String cpath=System.getProperty("java.class.path");
		System.out.println ("classpath "+ cpath);
		/*
		System.out.println("load Selection filters: setting classpath:  "+cp);
		System.setProperty("java.class.path", cp);
		*/
		ClassLoader classLoader= ClassifierManager.class.getClassLoader();

		for(String plugin: classes){
			//System.out.println("checking "+ plugin);
			try {
				Class<?>[] classesList=(classLoader.loadClass(plugin)).getInterfaces();

				for(Class<?> cs:classesList){
					// we load only IFeature classes
					//System.out.println(cs.getSimpleName());

					if (cs.getSimpleName().equals(ASCommon.IFEATURE) && !classLoader.loadClass(plugin).isInterface()){

						IFeatureSelection	ianno =(IFeatureSelection) (classLoader.loadClass(plugin)).newInstance(); 
						Pair<String, String> p=ianno.getKeyVal();
						String pkey=p.first;
						//System.out.println(" IFilter " + pkey);

						FilterType ft=ianno.getAType();
						System.out.println(pkey+ " class, type " + ft);
						IFeatureSelection	filter =(IFeatureSelection) ianno;
						//Map<String, String> fmap=filter.getAnotatedFileds();
						//	annotationMap.put(pkey, fmap);
						featureMap.put(pkey, filter);

					} 

				} // end for
			} catch (  Exception ex) {
				System.out.println("error:" + plugin +" not found");
			}

		} // end for
		System.out.println("end loading features");
		if (featureMap.isEmpty()) 
			throw new RuntimeException("Filter list empty. Check project file ");
	 
	}
	
 
	
	
	private  List<String> installJarPlugins(String plugin) throws IOException {
		List<String> classNames = new ArrayList<>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(plugin));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(ASCommon.DOTCLASS)) {
				String className = entry.getName().replace('/', '.'); // including ".class"
				classNames.add(className.substring(0, className.length() - ASCommon.DOTCLASS.length()));
			}
		}
		zip.close();
		return classNames;
	}

	
	/**
	 * trains the classifier
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
			
			IFeatureSelection cclass =featureMap.get(cname);
			outputstr+= currentClassifier.evaluateModel(dataset, cclass);
			 
			//Write output-> move to evaluation;
			InstanceUtil.writeDataToTXT(outputstr, projectInfo);
			
			// to avoid data creep
			dataset.delete();
		
		} catch (Exception e) {		
			e.printStackTrace();
		}
	}
	
	/**
	 * saves the learning metadata
	 */
	public void saveLearningMetaData(){	
		projectInfo= projectMan.getMetaInfo();
		projectMan.writeMetaInfo(projectInfo);		
	}

	/**
	 * gets the learning metadata
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
	}

	/**
	 * 
	 * @param dataSet
	 * @return
	 */
	public double[] applyClassifier(IDataSet dataSet){
			final int ni=dataSet.getNumInstances();
			double[] classificationResult = new double[ni];
			LearningInfo li= projectInfo.getLearning();
			String cname= li.getLearningOption();
			System.out.println("learning option "+ cname);
			ApplyTask applyTask=null;
			IDataSet fdata=null;
			if (cname!="")  {
				IFeatureSelection filter =featureMap.get(cname);
				//System.out.print("Classifier Manager: selecting feature " +filter. getName()+ " "+cname);
				//fdata=filter.selectFeatures(dataSet);
				fdata=filter.filterData(dataSet);
			}
			if (fdata!=null) {
				try {
					//IFeatureSelection filter =featureMap.get(cname);	
					//applyTask= new ApplyTask(fdata, 0, ni, classificationResult, currentClassifier, filter);
					applyTask= new ApplyTask(fdata, 0, ni, classificationResult, currentClassifier);
					//System.out.println("cname "+ cname);			 								 	
					//applyTask.setFilter(filter);
					pool.invoke(applyTask);
				} catch ( Exception ex) {
					System.out.println("Exception in applyClassifier ");
					ex.printStackTrace();
				}
			} else {
				System.out.println("Classifier Manager: applyClassifier: IDataSet fdata set is null"); 
				try {
					applyTask= new ApplyTask(dataSet, 0, ni, classificationResult, currentClassifier);
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
	public TreeMap<String,IFeatureSelection> getFeatureSelMap() {
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
