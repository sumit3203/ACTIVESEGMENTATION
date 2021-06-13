package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import activeSegmentation.ASCommon;
import activeSegmentation.IFilter;
import activeSegmentation.ProjectType;
import activeSegmentation.filter.ALoG_Filter_;
import activeSegmentation.filter.FilterManager;
import activeSegmentation.filter.Hessian_Filter_;
import activeSegmentation.filter.LoG_Filter_;
import activeSegmentation.filter.StructureT_Filter_;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.InstanceUtil;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class SegmentationTester {

	Map<String, IFilter> filtersMap= new HashMap<>();
	int numberOfFeatures=0;
	ArrayList<Attribute> attributes = new ArrayList<>();
	List<String> classlabels=new LinkedList<>();
	private InstanceUtil instanceUtil= new InstanceUtil();
	ProjectManager pm;
	FilterManager fm;
	ProjectInfo projectInfo;
	RandomForest randomForest= new RandomForest();
	//training Data
	Instances trainingData;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String trainingImage="C:\\Users\\vohra\\Documents\\deeplearning\\ISBI-DATASET\\small-dataset\\train.tif";
		String trainlabels="C:\\Users\\vohra\\Documents\\deeplearning\\ISBI-DATASET\\small-dataset\\train-label.tif";
		String testImage="C:\\Users\\vohra\\Documents\\deeplearning\\ISBI-DATASET\\small-dataset\\train.tif";
		String testlabels="C:\\Users\\vohra\\Documents\\deeplearning\\ISBI-DATASET\\small-dataset\\train-label.tif";
		String[] labels= {"black", "cell"};
		SegmentationTester tester= new SegmentationTester();
		tester.run(trainingImage, trainlabels, labels, testImage, testlabels);
	}

	public void run(String trainingImage, String trainlabels, String[] labels, String testImageTiff, String testLabelTiff) throws Exception {

		loadDataset(trainingImage, trainlabels);
		String trainDir=this.projectInfo.getProjectDirectory().get(ASCommon.K_IMAGESDIR);
		String featuresDir=this.projectInfo.getProjectDirectory().get(ASCommon.K_FILTERSDIR);

		applyFilters(trainDir, featuresDir);
		for(String cellType: labels) {
			classlabels.add(cellType);
		}
		ImagePlus traininglabels=new ImagePlus(trainlabels);	
		List<Instance> instances=createTrainingInstance(traininglabels);
		Collections.shuffle(instances);
		List<Instance> subinstances=instances.subList(0, 50000);
		List<Instance> trainInstances=subinstances.subList(0, subinstances.size()*4/5);
		List<Instance> testInstances=subinstances.subList(subinstances.size()*4/5, subinstances.size() );
		trainClassfier(trainInstances, testInstances);

		saveResult(trainDir, traininglabels, featuresDir);
		
		//testing
		String testImagesDir=this.projectInfo.getProjectDirectory().get(ASCommon.K_TESTIMAGESDIR);
		String testFilterDir=this.projectInfo.getProjectDirectory().get(ASCommon.K_TESTFILTERDIR);
		ImagePlus testImage=new ImagePlus(testImageTiff);
		createStackImage(testImage, ".tif", testImagesDir, testFilterDir);
		applyFilters(testImagesDir, testFilterDir);
		ImagePlus testLabels=new ImagePlus(testLabelTiff);
		saveResult(testImagesDir, testLabels, testFilterDir);
		
		

	}

	public void saveResult(String dir, ImagePlus currentImage, String featuresDir) throws Exception {
		String segPath = this.projectInfo.getProjectDirectory().get(ASCommon.K_FEATURESDIR);
		List<String>images= loadImages(dir);
		//now classificationResult has predictions of all pixels of one particular image
		for(String image: images) {
			System.out.println(image);

			ImagePlus testImage= new ImagePlus(dir+image);
			double[] classificationResult = new double[testImage.getWidth()*testImage.getHeight()];
			ImageStack featureStack= loadFeatureStack(featuresDir, image);

			
			int k=0;
			for( int y = 0; y < testImage.getHeight(); y++ )				
			{
				for( int x = 0; x < testImage.getWidth(); x++ ){

					Instance ins=instanceUtil.createInstance(x, y, 0,featureStack ,false, false);
					ins.setDataset(trainingData);
					double [] pred=randomForest.distributionForInstance(ins);
					classificationResult[k]=pred[0];
					k++;
				}
			}
			ImageProcessor classifiedSliceProcessor = new FloatProcessor(testImage.getWidth(),
					testImage.getHeight(), classificationResult);
			/**/
			ImagePlus classifiedImage = new ImagePlus(image, classifiedSliceProcessor);
			classifiedImage.setCalibration(currentImage.getCalibration());
			IJ.save(classifiedImage, segPath + image);
		}
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
		//SegmentationTester.System.out.println(trainingData);	
		//smo.setBatchSize("100");
		//	randomForest.setNumIterations(100);
		randomForest.setNumExecutionSlots(4);
		try {
			;
			Instances newdata = trainingData;
			System.out.println("training started");
			randomForest.buildClassifier(newdata);
			System.out.println("training ends");
			System.out.println(randomForest.toString());
			Evaluation eval = new Evaluation(newdata);
			eval.evaluateModel(randomForest, trainingData);
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval.toClassDetailsString());

		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public  List<Instance> createTrainingInstance(ImagePlus labelImage) {
		String projectString=this.projectInfo.getProjectDirectory().get(ASCommon.K_IMAGESDIR);
		String filterString=this.projectInfo.getProjectDirectory().get(ASCommon.K_FILTERSDIR);
		List<String>images= loadImages(projectString);
		List<Instance> instances= new ArrayList<>();
		ImageStack imageStack= labelImage.getImageStack();
		int i=1;
		for(String image: images) {
			System.out.println(image);
			if(i==1) {
				String dir=image.substring(0, image.lastIndexOf("."));
				createHeader(filterString+dir, classlabels);
			}
			//File[] imagestack=sortImages(new File(filterString+image.substring(0, image.lastIndexOf("."))).listFiles());

			ImageStack featureStack= loadFeatureStack(filterString, image);
			//loadFeatureStack(featurePath, imageName)
			List<Instance> tempInstances=createInstanceList(featureStack, imageStack.getProcessor(i));
			instances.addAll(tempInstances);
			i++;
		}

		return instances;
	}
	private List<Instance> createInstanceList(
			ImageStack featureStack, ImageProcessor classLabels) 
	{		

		List<Instance> instances= new ArrayList<>();

		for( int y = 0; y < featureStack.getHeight(); y++ )				
		{
			for( int x = 0; x < featureStack.getWidth(); x++ ){
				int pixel= classLabels.getPixel(x, y);
				int classLabel=0;
				if(pixel==255) {
					classLabel=1;
				}
				instances.add( instanceUtil.createInstance(x, y, classLabel,featureStack ,false, false) );

			}		
		}
		// increase number of instances for this class
		//System.out.println(testingData.get(1).toString());
		return instances;		
	}

	public void applyFilters(String imagesDir, String outDir){
		loadFilters();
		
		//System.out.println(projectString);
		List<String>images= loadImages(imagesDir);
		System.out.println(images);
		for(IFilter filter: filtersMap.values()){
			System.out.println("filter applied"+filter.getName());
			System.out.println(filter.isEnabled());
			if(filter.isEnabled()){
				for(String image: images) {
					//IJ.log(image);
					filter.applyFilter(new ImagePlus(imagesDir+image).getProcessor(),outDir+image.substring(0, image.lastIndexOf(".")), null);
				}

			}

		}


	}
	public ProjectInfo loadDataset(String trainingImage, String trainingLabel) {
		this.pm = new ProjectManager();
		String projectName="Test_Project";
		String projectDirectory="C:\\Users\\vohra\\Documents\\deeplearning\\";
		String projectDescription="test project";
		deleteDirectory(new File(projectDirectory+projectName));
		pm.createProject(projectName, ProjectType.SEGM.toString(),
				projectDirectory, projectDescription, trainingImage);
		this.projectInfo=pm.getMetaInfo();

		return projectInfo;
	}
	
	private boolean createDirectory(String project){
		File file=new File(project);
		if(!file.exists()){
			file.mkdirs();
		}
		return true;
	}

	
	private void createStackImage(ImagePlus image,String format, String testimagefolder, String filterFolder ) {
		IJ.log("createStack");
		//String format=image.getTitle().substring(image.getTitle().lastIndexOf("."));
		//String folder=image.getTitle().substring(0, image.getTitle().lastIndexOf("."));
		String imagename=image.getTitle();
		String folder=imagename.substring(0, imagename.lastIndexOf("."));
		IJ.log(format);
		for(int i=1; i<=image.getStackSize();i++) {
			ImageProcessor processor= image.getStack().getProcessor(i);
			String title= folder+i;
			IJ.log(folder);
			IJ.log(title);
			createDirectory(filterFolder+title);
			IJ.saveAs(new ImagePlus(title, processor),format, testimagefolder+title);
		}
		IJ.log("createStackdone");
	}	

	public void createHeader(String dir, List<String> classlabels) {
		File[] files=sortImages(new File(dir).listFiles());
		int featureSIndex=0;
		//featureIndex.put(key, featureSIndex);

		for(File file: files) {
			attributes.add(new Attribute(file.getName().substring(0, file.getName().lastIndexOf("."))));
			featureSIndex+=1;
		}

		numberOfFeatures=featureSIndex+1;
		attributes.add(new Attribute(ASCommon.CLASS,classlabels));
	}
	public Boolean deleteDirectory(File deleteDir) {
		File[] allfiles=deleteDir.listFiles();
		if(allfiles!=null) {
			for(File file : allfiles) {
				deleteDirectory(file);
			}
		}
		return deleteDir.delete();

	}
	public void loadFilters() {
		Hessian_Filter_ hessian_Filter_= new Hessian_Filter_();
		hessian_Filter_.setEnabled(true);
		LoG_Filter_ logfilter= new LoG_Filter_();
		logfilter.setEnabled(true);
		ALoG_Filter_ alogfilter= new ALoG_Filter_();
		alogfilter.setEnabled(true);
		StructureT_Filter_ structureT_Filter_= new StructureT_Filter_();
		filtersMap.put(hessian_Filter_.getKey(), hessian_Filter_);
		filtersMap.put(logfilter.getKey(), logfilter);
		filtersMap.put(alogfilter.getKey(), alogfilter);
		filtersMap.put(structureT_Filter_.getKey(), structureT_Filter_);

	}

	public void applyFilters(ImageProcessor image, String path, String imageName) {
		for(String key: filtersMap.keySet()) {
			IFilter filter= filtersMap.get(key);
			filter.applyFilter(image, path+imageName.substring(0, imageName.lastIndexOf(".")), null);
		}
	}

	private ImageStack loadFeatureStack(String featurePath,String imageName){
		String localPath=imageName.substring(0, imageName.lastIndexOf("."));
		//System.out.println(featurePath+images.get(0).substring(0, images.get(0).lastIndexOf(".")));

		File[] images=sortImages(new File(featurePath+localPath).listFiles());

		ImagePlus firstImage=IJ.openImage(featurePath+localPath+"/"+images[0].getName());
		ImageStack featureStack = new ImageStack(firstImage.getWidth(), firstImage.getHeight());
		for(File file : images){
			if (file.isFile()) {
				//System.out.println(file.getName());
				IJ.log(file.getName());
				ImagePlus image=IJ.openImage(featurePath+localPath+"/"+file.getName());

				featureStack.addSlice(image.getTitle(), image.getProcessor());

			}
		}
		return featureStack;
	}

	private File[] sortImages(File[] images) {
		final Pattern p = Pattern.compile("\\d+");
		Arrays.sort(images, new  Comparator<File>(){
			@Override public int compare(File o1, File o2) {
				Matcher m = p.matcher(o1.getName());
				Integer number1 = null;
				if (!m.find()) {
					return o1.getName().compareTo(o2.getName());
				}
				else {
					Integer number2 = null;
					number1 = Integer.parseInt(m.group());
					m = p.matcher(o2.getName());
					if (!m.find()) {
						return o1.getName().compareTo(o2.getName());
					}
					else {
						number2 = Integer.parseInt(m.group());
						int comparison = number1.compareTo(number2);
						if (comparison != 0) {
							return comparison;
						}
						else {
							return o1.getName().compareTo(o2.getName());
						}
					}
				}
			}}
				);
		return images;
	}

	private List<String> loadImages(String directory){
		List<String> imageList= new ArrayList<String>();
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}
		return imageList;
	}



}
