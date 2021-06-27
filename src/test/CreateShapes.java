package test;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import activeSegmentation.ASCommon;
import activeSegmentation.moment.Haralick_feature_;
import activeSegmentation.moment.Legendre_feature_;
import activeSegmentation.moment.Zernike_feature_;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class CreateShapes {

	//features
	Legendre_feature_ legendre_filter_=new Legendre_feature_();
	Haralick_feature_ glcm_Filter_= new Haralick_feature_();
	Zernike_feature_ zernike_Filter_= new Zernike_feature_();

	// create attribute list
	Map<String, Set<String>> featureNames= new HashMap<>();
	int numberOfFeatures=0;
	ArrayList<Attribute> attributes = new ArrayList<>();
	List<String> classlabels=new LinkedList<>();
	//classifier
	RandomForest randomForest= new RandomForest();
	//training Data
	Instances trainingData;

	public CreateShapes() {
		// create features
		legendre_filter_.generateFeatures();			
		glcm_Filter_.generateFeatures();			
		zernike_Filter_.generateFeatures();
		featureNames.put(legendre_filter_.getKey(), legendre_filter_.getFeatureNames());
		featureNames.put(glcm_Filter_.getKey(), glcm_Filter_.getFeatureNames());
		featureNames.put(zernike_Filter_.getKey(), zernike_Filter_.getFeatureNames());			
		classlabels.add("t");
		classlabels.add("c");
		createHeader(classlabels);
	}


	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new ImageJ();

		CreateShapes shapes= new CreateShapes();

       shapes.simpleTest();
		//shapes.complexTest();

	}


	public void complexTest() throws Exception {
		IJ.run("Blobs (25K)");
		ImageProcessor imageProcessor=IJ.getImage().getProcessor();
		int width=imageProcessor.getWidth();
		int height=imageProcessor.getHeight();
		int circles=100;
		int triangles=100;
		int widthRange1=10;
		int widthRange2=60;

		List<TestRoi> roiList=createTriangesAndOvals(circles, triangles, width, widthRange1, widthRange2, imageProcessor);
		Collections.shuffle(roiList);
		trainClassfier(imageProcessor.duplicate(), roiList);
		List<TestRoi> testRoiList=createTriangesAndOvals(50, 50, width, 5, 40, imageProcessor);
		Collections.shuffle(testRoiList);
		testIntances(testRoiList, imageProcessor);
	}

	public void simpleTest() throws Exception {
		int width=500;
		int height=500;
		int circles=100;
		int triangles=100;
		int widthRange1=10;
		int widthRange2=60;
		ImageProcessor imageProcessor= new FloatProcessor(width, height);
		//imageProcessor.setRoi(roi);
		//ImagePlus ip= new ImagePlus("tes", imageProcessor);
		//ip.show();
		//ImagePlus ip= new ImagePlus("test_circle", imageProcessor);
		//ip.show();
		List<TestRoi> roiList=createTriangesAndOvals(circles, triangles, width, widthRange1, widthRange2, imageProcessor);
		Collections.shuffle(roiList);
		trainClassfier(imageProcessor, roiList);
		List<TestRoi> testRoiList=createTriangesAndOvals(50, 50, width, 5, 40, imageProcessor);
		Collections.shuffle(testRoiList);
		testIntances(testRoiList, imageProcessor);
	}
	public  int[] combine(int[] a, int[] b){
		int length = a.length + b.length;
		int[] result = new int[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
	public double randDouble(double bound1, double bound2) {
		//make sure bound2> bound1
		double min = Math.min(bound1, bound2);
		double max = Math.max(bound1, bound2);
		//math.random gives random number from 0 to 1
		return min + (Math.random() * (max - min));
	}

	public List<TestRoi> createTriangesAndOvals(int circles, int triangles, int width, int widthRange1,
			int widthRange2, ImageProcessor imageProcessor){

		List<TestRoi> roiList= new ArrayList<>();
		for(int i=0; i<circles; i++) {
			int x= (int) randDouble(50, width-100);
			int y= (int) randDouble(50, width-100);
			int circleWidth=(int) randDouble(widthRange1, widthRange2);

			Roi roi= new OvalRoi(x, y, circleWidth, circleWidth);
			roi.setStrokeWidth(2);
			roi.drawPixels(imageProcessor);


			TestRoi testroi=new TestRoi(roi, "c");
			roiList.add(testroi);

		}
		for(int i=0; i<triangles; i++) {
			int x= (int) randDouble(50, width-100);
			int y= (int) randDouble(50, width-100);
			int dist1=(int) randDouble(widthRange1, widthRange2);
			int dist2=(int) randDouble(widthRange1, widthRange2);
			int x1= x+dist1;
			int y1= y+dist2;
			int x2= x1+dist1;
			int y2= x2+dist2;
			Line line1= new Line(x, y, x1, y1 );
			Line line2= new Line(x1, y1, x2, y2 );
			Line line3= new Line(x, y, x2, y2 );
			Polygon polygon1=line1.getPolygon();
			Polygon polygon2=line2.getPolygon();
			Polygon polygon3=line3.getPolygon();
			int[] xpointsmid=combine(polygon1.xpoints, polygon2.xpoints);
			int[] xpoints=combine(xpointsmid, polygon3.xpoints);
			int[] ypointsmid=combine(polygon1.ypoints, polygon2.ypoints);
			int[] ypoints=combine(ypointsmid, polygon3.ypoints);

			Roi  triangleRoi= new PolygonRoi(xpoints, ypoints, xpoints.length,Roi.FREEROI);
			triangleRoi.drawPixels(imageProcessor);
			TestRoi roi= new TestRoi(triangleRoi, "t");
			roiList.add(roi);

			//Roi roi= new Ro
		}
		ImagePlus image= new ImagePlus("", imageProcessor);
		image.show();
		return roiList;

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
	public List<Instance>  generateInstanceList(ImageProcessor image, List<TestRoi> trainRois) {
		// create class labels	
		Map<String, Integer> classIndexMap=new HashMap<>();
		int index=0;
		for(String label: classlabels) {
			classIndexMap.put(label, index);
			index++;
		}
		List<Instance> instanceList= new ArrayList<>();
		for(TestRoi cell : trainRois) {
			ImageProcessor imageProcessor=image.duplicate();
			Roi roi=cell.getRoi();

			imageProcessor.setRoi(roi);

			ImageProcessor ip_roi = imageProcessor.crop();
			Pair<String,double[]> features=legendre_filter_.apply(ip_roi,roi );
			Pair<String,double[]> glcmfeatures=glcm_Filter_.apply(ip_roi,roi);
			Pair<String,double[]> zfeatures=zernike_Filter_.apply(ip_roi,roi  );

			double[] featuresData=combine(features.second, glcmfeatures.second);
			double[] zfeaturesData=combine(featuresData, zfeatures.second);
			double[] classData;
			double classIndex[]= {classIndexMap.get(cell.getRoiType())};

			classData=combine(zfeaturesData, classIndex);



			Instance instance=new DenseInstance(1.0,classData);
			instanceList.add(instance);

		}
		return instanceList;
	}
	public void trainClassfier(ImageProcessor image, List<TestRoi> trainRois) throws Exception {

		List<Instance> trainInstances= generateInstanceList(image, trainRois);

		trainingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		trainingData.setClassIndex(numberOfFeatures-1);
		for(Instance instance:trainInstances) {
			trainingData.add(instance);
		}
		System.out.println(trainingData);	
		//smo.setBatchSize("100");
		randomForest.setNumIterations(100);
		try {
			randomForest.buildClassifier(trainingData);
			System.out.println(randomForest.toString());
			Evaluation eval = new Evaluation(trainingData);
			//eval.evaluateModel(smo, test);
			eval.crossValidateModel(randomForest, trainingData, 10,  new Random(1));
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval.toClassDetailsString());

		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testIntances(List<TestRoi> testRois,ImageProcessor image) throws Exception {
		List<Instance> testInstances= generateInstanceList(image, testRois);

		int cellcount=0;
		double totaltriangle=0;
		double totalcircles=0;
		double correctTriangles=0;
		double correctCircles=0;
		for(Instance instance: testInstances) {
			instance.setDataset(this.trainingData);
			double[] probability=randomForest.distributionForInstance(instance);
           
			if(probability[0]>probability[1] && instance.classValue()==0) {
				correctTriangles++;

			}

			if(probability[1]>probability[0] && instance.classValue()==1) {
				correctCircles++;

			}
			if(instance.classValue()==0) {
				totaltriangle++;
			}
			else {
				totalcircles++;
			}
		}

		double truePInh= correctTriangles/totaltriangle;
		double truePExh= correctCircles/totalcircles;
		double accuracy=((correctCircles+correctTriangles)/(totalcircles+totaltriangle));
		System.out.println("correct circles - " + truePInh);
		System.out.println("correct triangle - "+ truePExh);
		System.out.println("Accuracy - "+  accuracy);

	}

	public  double[] combine(double[] a, double[] b){
		int length = a.length + b.length;
		double[] result = new double[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
}
