package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import activeSegmentation.ASCommon;
import activeSegmentation.moment.Haralick_feature_;
import activeSegmentation.moment.IJstat_feature_;
import activeSegmentation.moment.Legendre_feature_;
import activeSegmentation.moment.Zernike_feature_;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class TestOnlineCells {
	//features
	Legendre_feature_ legendre_filter_=new Legendre_feature_();
	Haralick_feature_ glcm_Filter_= new Haralick_feature_();
	Zernike_feature_ zernike_Filter_= new Zernike_feature_();
	IJstat_feature_ imageJfeatures= new IJstat_feature_();
	// create attribute list
	Map<String, Set<String>> featureNames= new HashMap<>();
	int numberOfFeatures=0;
	ArrayList<Attribute> attributes = new ArrayList<>();
	List<String> classlabels=new LinkedList<>();
	//classifier
	SMO randomForest= new SMO();
	//training Data
	Instances trainingData;

	public TestOnlineCells() {
		// TODO Auto-generated constructor stub
		legendre_filter_.generateFeatures();			
		glcm_Filter_.generateFeatures();			
		zernike_Filter_.generateFeatures();
		featureNames.put(legendre_filter_.getKey(), legendre_filter_.getFeatureNames());
		featureNames.put(glcm_Filter_.getKey(), glcm_Filter_.getFeatureNames());
		featureNames.put(zernike_Filter_.getKey(), zernike_Filter_.getFeatureNames());	
		featureNames.put(imageJfeatures.getKey(), imageJfeatures.getFeatureNames());	

	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new ImageJ();
		TestOnlineCells test= new TestOnlineCells();
		String directory="C:\\Users\\vohra\\Documents\\EM\\hela\\";
		test.run(directory);
		//System.out.println(dirs);
	}

	public void run(String directory) throws Exception {
		Map<String,List<CellType>>cells=loadCells(directory);
		List<Instance> trainInstances= new ArrayList<>();
		List<Instance> testInstances= new ArrayList<>();
		for(String key: cells.keySet()) {
			List<CellType> cellsinside=cells.get(key);
			Collections.shuffle(cellsinside);
			for(CellType cell: cellsinside.subList(0, cellsinside.size()*4/5)) {
				trainInstances.add(cell.getInstances());
			}
			for(CellType cell: cellsinside.subList(cellsinside.size()*4/5,cellsinside.size())) {
				testInstances.add(cell.getInstances());
			}
		}
		Collections.shuffle(trainInstances);
		
		
		trainClassfier(trainInstances, testInstances);

	}

	public void trainClassfier(List<Instance> trainInstances, List<Instance> testInstances) throws Exception {


		//	List<Instance> trainInstances=instances.subList(0, instances.size()*4/5);
		//	List<Instance> testInstances=instances.subList(instances.size()*4/5, instances.size() );

		Instances testData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		testData.setClassIndex(numberOfFeatures-1);
		for(Instance instance:testInstances) {
			testData.add(instance);
		}
		trainingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		trainingData.setClassIndex(numberOfFeatures-1);
		for(Instance instance:trainInstances) {
			trainingData.add(instance);
		}
		System.out.println(trainingData);	
		//smo.setBatchSize("100");
	//	randomForest.setNumIterations(100);
		try {
			;
			Instances newdata = trainingData;
			
			randomForest.buildClassifier(newdata);
			System.out.println(randomForest.toString());
			Evaluation eval = new Evaluation(newdata);
			//eval.evaluateModel(smo, test);
			eval.crossValidateModel(randomForest, newdata, 10,  new Random(1));
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval.toClassDetailsString());
			Evaluation eval1 = new Evaluation(newdata);
			Instances testNewData = testData;
			eval1.evaluateModel(randomForest, testNewData);
			System.out.println(eval1.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval1.toClassDetailsString());

		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public Map<String,List<CellType>> loadCells(String directory){
		Map<String,List<CellType>> cells= new HashMap<>();
		Set<String> dirs=loadDirectory(directory);
		for(String cellType: dirs) {
			classlabels.add(cellType);
		}
		createHeader(classlabels);
		Map<String, Integer> classIndexMap=new HashMap<>();
		int index=0;
		for(String label: classlabels) {
			classIndexMap.put(label, index);
			index++;
		}
		for(String cellType: dirs) {
			//System.out.println();
			Set<String> cellsDir=loadImages(directory+cellType);
			List<CellType> cellList= new ArrayList<>();
			cells.put(cellType, cellList);
			for(String traincells: cellsDir) {
				String tiffile=directory+cellType+"\\"+traincells;
				Instance instance= createInstance(tiffile, cellType,classIndexMap);
				CellType cell= new CellType(tiffile, cellType, instance);
				List<CellType> currentList= cells.get(cellType);
				currentList.add(cell);
				cells.put(cellType, currentList);
				System.out.println(tiffile);
			}
			//System.out.println(cellsDir);
		}
		//System.out.println(cells);
		return cells;
	}
	public void createHeader(List<String> classlabels) {
		int featureSIndex=0;

		for(String key: featureNames.keySet()) {
			//featureIndex.put(key, featureSIndex);
			featureSIndex+=featureNames.get(key).size();
			for(String attribute: featureNames.get(key)) {
				attributes.add(new Attribute(attribute));
			}

		}
		numberOfFeatures=featureSIndex+1;
		attributes.add(new Attribute(ASCommon.CLASS,classlabels));

	}
	public  double[] combine(double[] a, double[] b){
		int length = a.length + b.length;
		double[] result = new double[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public Instance  createInstance(String tiff, String cellType, Map<String, Integer> classIndexMap) {

		ImagePlus currentImage=IJ.openImage(tiff);
		ImageProcessor ip_roi = currentImage.getProcessor();
		Roi roi=new Roi(0, 0, ip_roi.getWidth(), ip_roi.getHeight());
		Pair<String,double[]> features=legendre_filter_.apply(ip_roi,roi );
		Pair<String,double[]> glcmfeatures=glcm_Filter_.apply(ip_roi,roi);
		Pair<String,double[]> zfeatures=zernike_Filter_.apply(ip_roi,roi  );

		double[] featuresData=combine(features.second, glcmfeatures.second);
		double[] zfeaturesData=combine(featuresData, zfeatures.second);
		double[] ifeatures=imageJfeatures.apply(ip_roi );
		double[] finalfeatures=combine(zfeaturesData, ifeatures);
		double[] classData;
		double classIndex[]= {classIndexMap.get(cellType)};

		classData=combine(finalfeatures, classIndex);



		Instance instance=new DenseInstance(1.0,classData);

		return instance;


	}
	public Set<String> loadImages(String directory){
		Set<String> imageList= new HashSet<>();
		File folder = new File(directory);
		File[] images = folder.listFiles();

		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}

		return imageList;
	}

	public Set<String> loadDirectory(String directory){
		Set<String> imageList= new HashSet<>();
		File folder = new File(directory);
		File[] images = folder.listFiles();

		for (File file : images) {
			if (file.isDirectory()) {
				imageList.add(file.getName());
			}
		}

		return imageList;
	}


}
