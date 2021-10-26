package test;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import activeSegmentation.ASCommon;
import activeSegmentation.filter.LoG_Filter_;
import activeSegmentation.moment.GLCM_feature_;
import activeSegmentation.moment.ImageJRoiFeatures;
import activeSegmentation.moment.Legendre_feature_;
import activeSegmentation.moment.Zernike_feature_;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.io.RoiEncoder;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ijaux.scale.Pair;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.instance.Randomize;



public class CellsClassification {
	private Normalize filter;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new ImageJ();
		String jsonfile="C:\\Users\\vohra\\Documents\\EM\\roi_cells_new.json";
		//String tifffile="C:\\Users\\vohra\\Documents\\EM\\fish1EM_reverse.tif";
		String tifffile="C:\\Users\\vohra\\Documents\\EM\\log_filter_8bitt.tif";
		String trainingfile="C:\\Users\\vohra\\Documents\\EM\\training.arff";
		String outPath="C:\\Users\\vohra\\Documents\\EM\\log_filter\\";
		CellsClassification classification= new CellsClassification();
		classification.trainClassfier(jsonfile, tifffile, trainingfile);
		//classification.drawRois(jsonfile, tifffile);
		//classification.applyLog(tifffile,outPath);
		//classification.imageJFeatures(jsonfile, tifffile);

	}


	
	
	public void drawRois(String fileName, String tiffFile) {
		ImagePlus currentImage=IJ.openImage(tiffFile);
		ImageStack currentStack=currentImage.getImageStack();
		List<CellRoi>cellsRoiList=loadRoiJson(fileName);
		Map<Integer, ImageProcessor> currentMap=new HashMap<>();
		Map<Integer, List<Roi>> roiMap=new HashMap<>();
		for(int i=1; i<=currentStack.getSize(); i++) {
			ImageProcessor currentProcessor=currentStack.getProcessor(i);
			currentMap.put(i, currentProcessor);
			roiMap.put(i, new ArrayList<>());
		}
		for(CellRoi cell: cellsRoiList) {
			for(int z= cell.getZ(); z<= (cell.getZ()+ cell.getD()); z++) {

				if(z<=397) {
					ImageProcessor currentProcessor=currentMap.get(z);
					//ImageProcessor duplicate=currentMap.get(z).duplicate();
					//System.out.println(z);
					Roi roi= new Roi(cell.getY(), cell.getX(), 5, 5);
					//roi.setName(UUID.randomUUID().toString());
					//currentProcessor.draw(roi);
					Overlay overLay= new Overlay(roi);
					currentProcessor.drawOverlay(overLay);
					//List<Roi> tempList=roiMap.get(z);
					//tempList.add(roi);
					//duplicate.setRoi(roi);
					//ImageProcessor ip_roi=duplicate.crop();
					// ImageStatistics stats=ip_roi.getStatistics();
					// System.out.println(stats.mode);
					//roiMap.put(z, tempList);
					//currentMap.put(z, currentProcessor); 
				}
				else {
					System.out.println(z);   
				}


			}
		}
		ImageStack newStack=new ImageStack(currentImage.getWidth(), currentImage.getHeight());
		for(int i=1; i<=currentStack.getSize(); i++) {

			newStack.addSlice(currentMap.get(i));
		}
		ImagePlus roiImage=new ImagePlus("test", newStack);
		roiImage.show();
		for(int i=1; i<=currentStack.getSize(); i++) {

			List<Roi> tempList=roiMap.get(i);
			saveRois("C:\\Users\\vohra\\Documents\\EM\\roi_zip\\"+i+".zip", tempList);
		}
	}
	
	private static boolean saveRois(String filename, List<Roi> rois) {
		//	System.out.println(filename);
		DataOutputStream out = null;
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
			out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (Roi roi : rois) {
				//	System.out.println(roi.getName());
				zos.putNextEntry(new ZipEntry(roi.getName() + ".roi"));
				re.write(roi);
				out.flush();
			}
			out.close();
		} catch (IOException e) {

			return false;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
		return true;
	}
	
	public Instances filterInstance(Instances trainingData) {
		filter = new Normalize();
		//normalize.setInputFormat(randomdata);
		//Instances newdata = Filter.useFilter(randomdata, normalize);
		
		Instances filteredIns=null;
		// Apply filter
		try {
			filter.setInputFormat(trainingData);

			filteredIns = Filter.useFilter(trainingData, filter);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return filteredIns;
	}
	public void applyLog(String tiffFile, String filterPath) {
		ImagePlus currentImage=IJ.openImage(tiffFile);
		ImageStack currentStack=currentImage.getImageStack();
		LoG_Filter_ log= new LoG_Filter_();
		log.sz=2;
		for(int i=1; i<=currentStack.getSize(); i++) {
			ImageProcessor currentProcessor=currentStack.getProcessor(i);
			log.applyFilter(currentProcessor, filterPath, i);
		}
		
	}

	public List<CellRoi> selectRois(List<CellRoi>cellsRoiList, int startSlice, int endSlice){
		List<CellRoi> newCellsList= new ArrayList<>();
		for(CellRoi roi: cellsRoiList) {
			if(roi.getZ()>=startSlice && roi.getZ()<=endSlice) {
				newCellsList.add(roi);
			}
		}
		
		return newCellsList;
		
	}
	public void trainClassfier(String fileName, String tiffFile, String trainingfile) throws Exception {
		ImagePlus currentImage=IJ.openImage(tiffFile);
		ImageStack currentStack=currentImage.getImageStack();
		List<CellRoi>fullRoiList=loadRoiJson(fileName);
		List<CellRoi>cellsRoiList=selectRois(fullRoiList, 41, 43);
		Collections.shuffle(cellsRoiList);
        System.out.println(cellsRoiList.size());
		List<CellRoi>trainRois=cellsRoiList.subList(0, cellsRoiList.size()*4/5);
		 System.out.println(trainRois.size());

		List<CellRoi>testRois=cellsRoiList.subList(cellsRoiList.size()*4/5,cellsRoiList.size());

		// create filters
		Legendre_feature_ legendre_filter_=new Legendre_feature_();
		legendre_filter_.generateFeatures();
		GLCM_feature_ glcm_Filter_= new GLCM_feature_();
		glcm_Filter_.generateFeatures();
		Zernike_feature_ zernike_Filter_= new Zernike_feature_();
		zernike_Filter_.generateFeatures();
		ImageJRoiFeatures imageJfeatures= new ImageJRoiFeatures();
		
		// create attribute list
		Map<String, Set<String>> featureNames= new HashMap<>();

		featureNames.put(legendre_filter_.getKey(), legendre_filter_.getFeatureNames());
		featureNames.put(glcm_Filter_.getKey(), glcm_Filter_.getFeatureNames());
		featureNames.put(zernike_Filter_.getKey(), zernike_Filter_.getFeatureNames());
		featureNames.put(imageJfeatures.getKey(), imageJfeatures.getFeatureNames());

		int featureSIndex=0;
		int numberOfFeatures=0;
		ArrayList<Attribute> attributes = new ArrayList<>();
		for(String key: featureNames.keySet()) {
			//featureIndex.put(key, featureSIndex);
			featureSIndex+=featureNames.get(key).size();
			for(String attribute: featureNames.get(key)) {
				attributes.add(new Attribute(attribute));
			}

		}
		numberOfFeatures=featureSIndex+1;
		// create class labels
		List<String> classlabels=new ArrayList<>();
		classlabels.add("i");
		classlabels.add("e");
		Map<String, Integer> classIndexMap=new HashMap<>();
		int index=0;
		for(String label: classlabels) {
			classIndexMap.put(label, index);
			index++;
		}

		// create instances
		List<Instance> inhinstances= new ArrayList<>();
		List<Instance> exhinstances= new ArrayList<>();
		int cellcount=0;
		int inhcount=0, exhcount=0;
		double min_area=1000000; double max_area=0;
		for(CellRoi cell : trainRois) {
			List<Instance> roiInstances= new ArrayList<>();
			for(int z= cell.getZ(); z<= (cell.getZ()+ cell.getD()); z++) {

				if(z<=397) {
				ImageProcessor currentProcessor=currentStack.getProcessor(z).duplicate();
				//System.out.println("wand run");
				Wand w = new Wand(currentProcessor);
				w.autoOutline(cell.getY(), cell.getX());
				
				if(w.npoints>0) {
				//	System.out.println("in");
				Roi roi = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, Roi.TRACED_ROI);
				
				//Roi roi= new Roi(cell.getY(), cell.getX(), 5, 5);
				roi.setName("test-"+cellcount);
				//System.out.println(roi);
				currentProcessor.setRoi(roi);
				ImageProcessor ip_roi = currentProcessor.crop();
				ImageStatistics stats=ip_roi.getStatistics();
				double roiarea=stats.area;
				if(roiarea>25 && roiarea<2000) {
					//min_area=roiarea;
				
				
				//ip_roi.autoThreshold();
				
				Pair<String,double[]> features=legendre_filter_.apply(ip_roi.duplicate(),roi );
				Pair<String,double[]> glcmfeatures=glcm_Filter_.apply(ip_roi.duplicate(),roi);
				Pair<String,double[]> zfeatures=zernike_Filter_.apply(ip_roi.duplicate(),roi  );

				
				double[] featuresData=combine(features.second, glcmfeatures.second);
				double[] zfeaturesData=combine(featuresData, zfeatures.second);
				double[] ifeatures=imageJfeatures.apply(ip_roi );
				double[] finalfeatures=combine(zfeaturesData, ifeatures);
      

				double classIndex[]= {classIndexMap.get(cell.getCelltype())};

				double[] classData=combine(finalfeatures, classIndex);

				Instance instance=new DenseInstance(1.0,classData);
				if(cell.getCelltype().equalsIgnoreCase("i")) {
					//instance.setWeight(3);
					inhcount++;
					inhinstances.add(instance);
				}
				if(cell.getCelltype().equalsIgnoreCase("e")) {
					//instance.setWeight(1.2);
					exhcount++;
					exhinstances.add(instance);
				}

				roiInstances.add(instance);
				}
				
				}

			}
			

			}
			cellcount++;
			System.out.println(cellcount);
			cell.setRoiInstances(roiInstances);

		}
		System.out.println("roi min area: "+min_area);
		System.out.println("roi max area: "+max_area);
		attributes.add(new Attribute(ASCommon.CLASS,classlabels));
		Instances trainingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		trainingData.setClassIndex(numberOfFeatures-1);
		for(Instance instance:inhinstances) {
			trainingData.add(instance);
		}
		Collections.shuffle(exhinstances);
		System.out.println("inh size-"+inhinstances.size());
		List<Instance>  newlist=exhinstances.subList(0, inhinstances.size());
		System.out.println("exh size-"+newlist.size());
		for(Instance instance:newlist) {
			trainingData.add(instance);
		}
		writeDataToARFF(trainingData, trainingfile);
         
		//Randomize randomize= new Randomize();
		//randomize.setInputFormat(trainingData);
		//Instances randomdata = Filter.useFilter(trainingData, randomize);
		Instances newdata=filterInstance(trainingData); 
		//Normalize normalize = new Normalize();
		//normalize.setInputFormat(randomdata);
		//Instances newdata = Filter.useFilter(randomdata, normalize);
		//System.out.println(trainingData.toSummaryString());
	    IBk smo= new IBk();
	    smo.setKNN(3);
		//RandomForest smo= new RandomForest();
		//smo.setBatchSize("200");
		//smo.setNumIterations(200);
		try {
			smo.buildClassifier(newdata);
			System.out.println(smo.toString());
			Evaluation eval = new Evaluation(newdata);
			//eval.evaluateModel(smo, test);
			eval.crossValidateModel(smo, newdata, 10,  new Random(1));
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval.toClassDetailsString());


			testIntanceNew(trainRois, currentStack, smo, trainingData );
			testIntance(testRois, currentStack, smo, trainingData);



		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(inhcount);
		System.out.println(exhcount);
	}

	public double[] combine(double[] a, double[] b){
		int length = a.length + b.length;
		double[] result = new double[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public List<CellRoi> loadRoiJson(String fileName) {

		ObjectMapper mapper = new ObjectMapper();
		List<CellRoi> cellrois;
		try {
			//System.out.println(fileName);
			File projectFile=new File(fileName);
			cellrois= mapper.readValue(projectFile, new TypeReference<List<CellRoi>>(){});
			return cellrois;
		} catch (UnrecognizedPropertyException e) {
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		return new ArrayList<>();

	}

	public boolean writeDataToARFF(Instances data, String filename)
	{
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream( filename ) ) );

			final Instances header = new Instances(data, 0);
			out.write(header.toString());

			for(int i = 0; i < data.numInstances(); i++)
			{
				out.write(data.get(i).toString()+"\n");
			}
		}
		catch(Exception e)
		{
			IJ.log("Error: couldn't write instances into .ARFF file.");
			IJ.showMessage("Exception while saving data as ARFF file");
			e.printStackTrace();
			return false;
		}
		finally{
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;

	}

	public void testIntanceNew(List<CellRoi> trainRois,ImageStack currentStack, Classifier classifier, Instances dataset ) throws Exception {

		Legendre_feature_ legendre_filter_=new Legendre_feature_();
		legendre_filter_.generateFeatures();
		GLCM_feature_ glcm_Filter_= new GLCM_feature_();
		glcm_Filter_.generateFeatures();
		Zernike_feature_ zernike_Filter_= new Zernike_feature_();
		zernike_Filter_.generateFeatures();

		int cellcount=0;
		double totalihb=0;
		double totalexh=0;
		double correctIhb=0;
		double correctExh=0;
		for(CellRoi cell : trainRois) {

			double inhP=0.0f, exhP=0.0f;
			for(Instance instance: cell.getRoiInstances()) {


				instance.setDataset(dataset);
				Instances instances=new Instances(dataset);
				instances.add(instance);
				Instances filInstance= Filter.useFilter(instances, filter);
				double[] probability=classifier.distributionForInstance(filInstance.lastInstance());
				//System.out.println(probability[0]+" -- "+ probability[1]);
				inhP+=probability[0];
				exhP+=probability[1];			
			}
			//System.out.println(inhP+" ---- "+ exhP + " --- "+ cell.getCelltype());
			if(inhP>exhP && cell.getCelltype().equalsIgnoreCase("i")) {
				correctIhb++;

			}

			if(exhP>inhP && cell.getCelltype().equalsIgnoreCase("e")) {
				correctExh++;
				//System.out.println("in e");
			}

			if(cell.getCelltype().equalsIgnoreCase("i")) {
				totalihb++;
			}
			else {
				totalexh++;
			}

		}
		double truePInh= correctIhb/totalihb;
		double truePExh= correctExh/totalexh;
		double accuracy=((correctIhb+correctExh)/(totalexh+totalihb));
		System.out.println("correct ihb - " + truePInh);
		System.out.println("correct exb - "+ truePExh);
		System.out.println("Accuracy - "+  accuracy);

	}

	public void testIntance(List<CellRoi> trainRois,ImageStack currentStack, Classifier classifier, Instances dataset ) throws Exception {

		Legendre_feature_ legendre_filter_=new Legendre_feature_();
		legendre_filter_.generateFeatures();
		GLCM_feature_ glcm_Filter_= new GLCM_feature_();
		glcm_Filter_.generateFeatures();
		Zernike_feature_ zernike_Filter_= new Zernike_feature_();
		zernike_Filter_.generateFeatures();
		ImageJRoiFeatures imageJfeatures= new ImageJRoiFeatures();
		int cellcount=0;
		double totalihb=0;
		double totalexh=0;
		double correctIhb=0;
		double correctExh=0;
		for(CellRoi cell : trainRois) {

			double inhP=0.0f, exhP=0.0f;
			for(int z= cell.getZ(); z<= (cell.getZ()+ cell.getD()); z++) {


				ImageProcessor currentProcessor=currentStack.getProcessor(z).duplicate();
				//Roi roi= new Roi(cell.getX(), cell.getY(), cell.getW(), cell.getH());
				Wand w = new Wand(currentProcessor);
				w.autoOutline(cell.getY(), cell.getX());
				if(w.npoints>0) {
					
				Roi roi = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, Roi.TRACED_ROI);
				
				//Roi roi= new Roi(cell.getY(), cell.getX(), 5,5);
				roi.setName("test-"+cellcount);
				//System.out.println(roi);
				currentProcessor.setRoi(roi);
				ImageProcessor ip_roi = currentProcessor.crop();
				//ImageProcessor ip_roi = currentProcessor.crop();
				ImageStatistics stats=ip_roi.getStatistics();
				double roiarea=stats.area;
				if(roiarea>25 && roiarea<2000) {
				//ip_roi.autoThreshold();
				Pair<String,double[]> features=legendre_filter_.apply(ip_roi.duplicate(),roi );
				Pair<String,double[]> glcmfeatures=glcm_Filter_.apply(ip_roi.duplicate(),roi);
				Pair<String,double[]> zfeatures=zernike_Filter_.apply(ip_roi.duplicate(),roi  );

				double[] featuresData=combine(features.second, glcmfeatures.second);
				double[] zfeaturesData=combine(featuresData, zfeatures.second);
				double[] ifeatures=imageJfeatures.apply(ip_roi );
				double[] finalfeatures=combine(zfeaturesData, ifeatures);
      

				double classIndex[]= {0};
				double[] classData=combine(finalfeatures, classIndex);

           
				Instance instance=new DenseInstance(1.0,classData);
				instance.setDataset(dataset);
				Instances instances=new Instances(dataset);
				instances.add(instance);
				Instances filInstance= Filter.useFilter(instances, filter);
				double[] probability=classifier.distributionForInstance(filInstance.lastInstance());
				//System.out.println(probability[0]+" -- "+ probability[1]);
				inhP+=probability[0];
				exhP+=probability[1];
				
				}
			}


			}
			//System.out.println(inhP+" ---- "+ exhP + " --- "+ cell.getCelltype());
			if(inhP>exhP && cell.getCelltype().equalsIgnoreCase("i")) {
				correctIhb++;

			}

			if(exhP>inhP && cell.getCelltype().equalsIgnoreCase("e")) {
				correctExh++;
				//System.out.println("in e");
			}

			if(cell.getCelltype().equalsIgnoreCase("i")) {
				totalihb++;
			}
			else {
				totalexh++;
			}

		}

		double truePInh= correctIhb/totalihb;
		double truePExh= correctExh/totalexh;
		double accuracy=((correctIhb+correctExh)/(totalexh+totalihb));
		System.out.println("correct ihb - " + truePInh);
		System.out.println("correct exb - "+ truePExh);
		System.out.println("Accuracy - "+  accuracy);

	}
}
