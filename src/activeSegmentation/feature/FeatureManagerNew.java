package activeSegmentation.feature;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import weka.core.Instance;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import activeSegmentation.Common;
import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.IProjectManager;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureManager;
import activeSegmentation.ILearningManager;
import activeSegmentation.IFeature;
import activeSegmentation.LearningType;
import activeSegmentation.ProjectType;
import activeSegmentation.gui.Util;
import activeSegmentation.io.ClassInfo;
import activeSegmentation.io.FeatureInfo;
import activeSegmentation.io.ProjectInfo;
import activeSegmentation.learning.ClassifierManager;

/**
 * 
 * 
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents Feature Manager to store , update and delete Samples , It also
 *           consist of code to load from metafile
 * 
 * 
 * @license This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 2.1 of the
 *          License, or (at your option) any later version.
 *
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details.
 *
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not, write to the Free Software
 *          Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *          USA
 */
public class FeatureManagerNew implements IFeatureManagerNew {

	private IProjectManager projectManager;
	private ProjectInfo projectInfo;
	private Random rand = new Random();
	private String projectString, featurePath;
	private int sliceNum, totalSlices;
	private List<String> images;
	private Map<String, IFeature> featureMap = new HashMap<String, IFeature>();
	private static RoiManager roiman = new RoiManager();
	private Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
	private List<Color> defaultColors;
	ILearningManager learningManager;
	private Map<String,Integer> predictionResultClassification;

	public FeatureManagerNew(IProjectManager projectManager) {
		this.projectManager = projectManager;
		this.projectInfo = this.projectManager.getMetaInfo();
		this.images = new ArrayList<String>();
		this.projectString = this.projectInfo.getProjectDirectory().get(Common.IMAGESDIR);
		//System.out.println(this.projectString);
		this.featurePath = this.projectInfo.getProjectDirectory().get(Common.FEATURESDIR);
		this.totalSlices = loadImages(this.projectString);
		this.defaultColors = Util.setDefaultColors();
		if (this.totalSlices > 0) {
			this.sliceNum = 1;
		}
		if (!setFeatureMetadata()) {
			for (int i = 1; i <= projectInfo.getClasses(); i++) {
				addClass();
			}
		}
		roiman.hide();
		learningManager = new ClassifierManager(projectManager);
		featureMap.put("SEGMENTATION", new PixelInstanceCreator(projectInfo));
		featureMap.put("CLASSIFICATION", new RoiInstanceCreator(projectInfo));
	}

	private int loadImages(String directory) {
		this.images.clear();
		File folder = new File(directory);
		File[] images = folder.listFiles();
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
		for (File file : images) {
			//System.out.println(file.getName());
			if (file.isFile()) {
				this.images.add(file.getName());
			}
		}
		return this.images.size();
	}

	@Override
	public void addExample(String key, Roi roi, String type) {
		String imageKey = this.images.get(sliceNum - 1);
		if (LearningType.valueOf(type).equals(LearningType.TESTING)) {
			classes.get(key).addTestingRois(imageKey, roi);
		} else {
			classes.get(key).addTrainingRois(imageKey, roi);
		}
		roiman.addRoi(roi);
	}

	@Override
	public void addExampleList(String classNum, List<Roi> roiList, String type) {
		for (Roi roi : roiList) {
			if (processibleRoi(roi)) {
				addExample(classNum, roi, type);
			}
		}
	}

	private boolean processibleRoi(Roi roi) {
		boolean ret = (roi != null && !(roi.getType() == Roi.LINE || roi.getType() == Roi.POLYLINE
				|| roi.getType() == Roi.ANGLE || roi.getType() == Roi.FREELINE || roi.getType() == Roi.POINT));
		return ret;
	}

	@Override
	public void deleteExample(String key, int index, String type) {
		String imageKey = this.images.get(sliceNum - 1);
		if (LearningType.valueOf(type).equals(LearningType.TESTING)) {
			classes.get(key).deleteTestingRoi(imageKey, index);
		} else {
			classes.get(key).deleteTrainingRoi(imageKey, index);
		}

	}

	@Override
	public List<Roi> getExamples(String key, String type) {
		String imageKey = this.images.get(sliceNum - 1);
		if (LearningType.valueOf(type).equals(LearningType.TESTING)) {
			return classes.get(key).getTestingRois(imageKey);
		} else {
			return classes.get(key).getTrainingRois(imageKey);
		}

	}

	@Override
	public Roi getRoi(String key, int index, String type) {
		String imageKey = this.images.get(sliceNum - 1);
		if (LearningType.valueOf(type).equals(LearningType.TESTING)) {
			return classes.get(key).getTestingRoi(imageKey, index);
		} else {
			return classes.get(key).getTrainingRoi(imageKey, index);
		}
	}

	private void sortMap() {

		 List<Map.Entry<String, ClassInfo> > list = 
	               new LinkedList<Map.Entry<String, ClassInfo> >(classes.entrySet()); 
	  
		final Pattern p = Pattern.compile("\\d+");
		Collections.sort(list, new  Comparator<Map.Entry<String, ClassInfo>>(){
		    @Override public int compare(Map.Entry<String, ClassInfo> o1,  
                    Map.Entry<String, ClassInfo> o2) {
		    	   Matcher m = p.matcher(o1.getValue().getLabel());
		           Integer number1 = null;
		           if (!m.find()) {
		               return o1.getValue().getLabel().compareTo(o2.getValue().getLabel());
		           }
		           else {
		               Integer number2 = null;
		               number1 = Integer.parseInt(m.group());
		               m = p.matcher(o2.getValue().getLabel());
		               if (!m.find()) {
		            	   return o1.getValue().getLabel().compareTo(o2.getValue().getLabel());
		               }
		               else {
		                   number2 = Integer.parseInt(m.group());
		                   int comparison = number1.compareTo(number2);
		                   if (comparison != 0) {
		                       return comparison;
		                   }
		                   else {
		                	   return o1.getValue().getLabel().compareTo(o2.getValue().getLabel());
		                   }
		               }
		           }
		    }}
		);
		
		//System.out.println(Arrays.toString(list));
	}
	@Override
	public Set<String> getClassKeys() {

		return classes.keySet();
	}

	@Override
	public String getClassLabel(String index) {
		return classes.get(index).getLabel();
	}

	@Override
	public int getRoiListSize(String key, String learningType) {
		String imageKey = this.images.get(sliceNum - 1);
		if (LearningType.valueOf(learningType).equals(LearningType.TESTING)) {
			return classes.get(key).getTestingRoiSize(imageKey);
		} else {
			return classes.get(key).getTrainingRoiSize(imageKey);
		}
	}
	
	
	@Override
	public List<Roi> getExamples(String key, String type, String imageKey) {
		//System.out.println(key +"----"+type+"----"+imageKey);
		if(LearningType.valueOf(type).equals(LearningType.BOTH)){
			List<Roi> roiList=new ArrayList<Roi>();
			if( classes.get(key).getTestingRois(imageKey)!=null) {
				roiList.addAll(classes.get(key).getTestingRois(imageKey));
			}
			if( classes.get(key).getTrainingRois(imageKey)!=null) {
				roiList.addAll(classes.get(key).getTrainingRois(imageKey));
			}
			//System.out.println(roiList.size());
			return roiList;
		}
		else if(LearningType.valueOf(type).equals(LearningType.TESTING)){
			return classes.get(key).getTestingRois(imageKey);
		}
		else{
			return classes.get(key).getTrainingRois(imageKey);
		}
	}


	@Override
	public void setClassLabel(String key, String label) {

		classes.get(key).setLabel(label);
	}

	@Override
	public int getNumOfClasses() {
		return classes.size();
	}

	@Override
	public void addClass() {
		String key = UUID.randomUUID().toString();
		if (!classes.containsKey(key)) {
			Map<String, List<Roi>> trainingRois = new HashMap<String, List<Roi>>();
			Map<String, List<Roi>> testingRois = new HashMap<String, List<Roi>>();
			ClassInfo classInfo = new ClassInfo(key, "label" + classes.size(), getColor(classes.size()), trainingRois,
					testingRois);
			classes.put(key, classInfo);
			sortMap();
		}
	}

	private Color getColor(int number) {
		if (number < defaultColors.size()) {
			return defaultColors.get(number);
		} else {
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			Color randomColor = new Color(r, g, b);
			return randomColor;
		}

	}

	@Override
	public void deleteClass(String key) {
		classes.remove(key);
	}

	public boolean setFeatureMetadata() {
		boolean alreadysetClass = false;
		projectInfo = projectManager.getMetaInfo();
		for (FeatureInfo featureInfo : projectInfo.getFeatureList()) {
			alreadysetClass = true;
			Map<String, List<Roi>> trainingRois = loadRois(featureInfo.getZipFile(), featureInfo.getTrainingList());
			Map<String, List<Roi>> testingRois = loadRois(featureInfo.getZipFile(), featureInfo.getTestingList());
			ClassInfo classInfo = new ClassInfo(featureInfo.getKey(), featureInfo.getLabel(),
					new Color(featureInfo.getColor()), trainingRois, testingRois);
			classes.put(featureInfo.getKey(), classInfo);
		}
		return alreadysetClass;
	}

	private Map<String, List<Roi>> loadRois(String filename, Map<String, List<String>> roiMapper) {
		Map<String, List<Roi>> roiMap = new HashMap<String, List<Roi>>();
		List<Roi> classRoiList = openZip(featurePath + filename);
		for (String imageKey : roiMapper.keySet()) {
			roiMap.put(imageKey, getRois(classRoiList, roiMapper.get(imageKey)));
		}

		return roiMap;
	}

	private List<Roi> getRois(List<Roi> classRoiList, List<String> roiNames) {
		List<Roi> roiList = new ArrayList<Roi>();
		for (String name : roiNames) {			
			for (Roi roi : classRoiList) {				
				if (roi.getName().equalsIgnoreCase(name)) {
					roiList.add(roi);
					
				}
			}
		}

		return roiList;
	}

	@Override
	public void saveFeatureMetadata() {
		projectInfo = projectManager.getMetaInfo();
		projectInfo.resetFeatureInfo();
		for (ClassInfo classInfo : classes.values()) {
			List<Roi> classRois = new ArrayList<Roi>();
			FeatureInfo featureInfo = new FeatureInfo();
			featureInfo.setKey(classInfo.getKey());
			featureInfo.setLabel(classInfo.getLabel());
			featureInfo.setColor(classInfo.getColor().getRGB());
			for (String imageKey : classInfo.getTrainingRoiSlices()) {
				List<String> trainingRois = new ArrayList<String>();
				for (Roi roi : classInfo.getTrainingRois(imageKey)) {
					trainingRois.add(roi.getName());
				}
				featureInfo.addTrainingRois(imageKey, trainingRois);
				classRois.addAll(classInfo.getTrainingRois(imageKey));
			}
			for (String imageKey : classInfo.getTestingRoiSlices()) {
				List<String> testingRois = new ArrayList<String>();
				for (Roi roi : classInfo.getTestingRois(imageKey)) {
					testingRois.add(roi.getName());
				}
				featureInfo.addTrainingRois(imageKey, testingRois);
				classRois.addAll(classInfo.getTestingRois(imageKey));
			}

			String fileName = Common.ROISET + classInfo.getKey() + Common.FORMAT;
			if (classRois != null & classRois.size() > 0) {				
				saveRois(featurePath + "/" + fileName, classRois);
				featureInfo.setZipFile(fileName);
			}
			projectInfo.addFeature(featureInfo);
		}

		projectManager.writeMetaInfo(projectInfo);
	}

	@Override
	public IDataSet extractFeatures(String featureType) {
       // System.out.println(featureType);
		featureMap.get(featureType).createTrainingInstance(classes.values());
		IDataSet dataset = featureMap.get(featureType).getDataSet();
		projectManager.setData(dataset);
		return dataset;
	}

	@Override
	public Set<String> getFeatures() {

		return null;
	}

	@Override
	public void addFeatures(IFeature feature) {

	}

	@Override
	public List<IDataSet> extractAll(String featureType) {

		return null;
	}

	@Override
	public boolean saveExamples(String filename, String classKey, String type) {
		//System.out.println(classKey + type);
		List<Roi> rois = getExamples(classKey, type);
		//System.out.println(rois.size());
		return saveRois(filename, rois);
	}

	private boolean saveRois(String filename, List<Roi> rois) {
		DataOutputStream out = null;
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
			out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (Roi roi : rois) {
				//System.out.println(roi.getName());
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

	@Override
	public void uploadExamples(String fileName, String classKey, String type) {

		addExampleList(classKey, openZip(fileName), type);

	}

	private List<Roi> openZip(String fileName) {
		Hashtable rois = new Hashtable();
		ZipInputStream in = null;
		List<Roi> roiList = new ArrayList<Roi>();
		ByteArrayOutputStream out = null;
		int nRois = 0;
		try {
			in = new ZipInputStream(new FileInputStream(fileName));
			byte[] buf = new byte[1024];
			int len;
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				if (name.endsWith(".roi")) {
					out = new ByteArrayOutputStream();
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					out.close();
					byte[] bytes = out.toByteArray();
					RoiDecoder rd = new RoiDecoder(bytes, name);
					Roi roi = rd.getRoi();
					if (roi != null) {
						name = name.substring(0, name.length() - 4);
						name = getUniqueName(name, rois);
						rois.put(name, roi);
						roiList.add(roi);
						nRois++;
					}
				}
				entry = in.getNextEntry();
			}
			in.close();
		} catch (IOException e) {

		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
		if (nRois == 0)
			System.out.println("ERROR OCCURED");
			
		return roiList;
		
	}

	private String getUniqueName(String name, Hashtable rois) {
		String name2 = name;
		int n = 1;
		Roi roi2 = (Roi) rois.get(name2);
		while (roi2 != null) {
			roi2 = (Roi) rois.get(name2);
			if (roi2 != null) {
				int lastDash = name2.lastIndexOf("-");
				if (lastDash != -1 && name2.length() - lastDash < 5)
					name2 = name2.substring(0, lastDash);
				name2 = name2 + "-" + n;
				n++;
			}
			roi2 = (Roi) rois.get(name2);
		}
		return name2;
	}
	
	
	public ImagePlus stackedClassifiedImage() {
		File[] files=finder(featurePath);
		ImageStack imageStack=null;
		
		for(int i=0 ; i<files.length;i++) {
			System.out.println(files[i].getName());
			ImagePlus image = new ImagePlus(featurePath+files[i].getName());
			if(i==0) {
				imageStack= new ImageStack(image.getWidth(), image.getHeight());
				imageStack.addSlice(image.getProcessor());
			}
			imageStack.addSlice(image.getProcessor());
		}
		return new ImagePlus("Segmented Image", imageStack);
	}
	
	 public File[] finder( String dirName){
	        File dir = new File(dirName);

	        return dir.listFiles(new FilenameFilter() { 
	                 public boolean accept(File dir, String filename)
	                      {return filename.toLowerCase().endsWith(".tif"); }
	        } );

	    }
	private int getRoiPredictionForClassification(Roi roi) {
		//actually have to use learningManager.predict(roi-- here we should have instance of roi);
		//System.out.println(roi.getName());
		Instance instance= featureMap.get(ProjectType.valueOf(projectInfo.getProjectType()).toString()).createInstance(roi);
		//System.out.println(instance.toString());
		return (int) learningManager.predict(instance);
		//return getDummyPrediction();
	}
	
	public Map<String,Integer> getClassificationResultMap(){
		if (predictionResultClassification==null) {
			compute();
		}		
		return predictionResultClassification;
	}

	@Override
	public ImagePlus compute() {
		
		if(ProjectType.valueOf(projectInfo.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
			predictionResultClassification = new HashMap<>();
		}		
		// IJ.debugMode=true;
		IJ.log("TRAINING STARTED");
		// extract features in weka format, returns IDataset object
		extractFeatures(ProjectType.valueOf(projectInfo.getProjectType()).toString());
		
		// trains as per the setting of learning manager, we now have a trained classifier
		learningManager.trainClassifier();
		
		IJ.log("TRAINING DONE");
		
		for (String image : images) {
			//System.out.println(image +" image");
																	
			//classification setting
			if(ProjectType.valueOf(projectInfo.getProjectType()).equals(ProjectType.CLASSIFICATION)) {															
							
				//list of rois to make classified image instance				
				List<Roi> training_roi_list;
				List<Roi> testing_roi_list;
																		
				// iterate over all training rois (of all the classes) and predict their output
				for(String key: getClassKeys()){										
					training_roi_list = classes.get(key).getTrainingRois(image);
					if(training_roi_list!=null) {
						for (Roi roi:training_roi_list) {																									
							predictionResultClassification.put(roi.getName(), getRoiPredictionForClassification(roi));
						}
					}					
				}				
				
				// iterate over all testing rois (of all the classes) and predict their output
				for(String key: getClassKeys()){					
					testing_roi_list = classes.get(key).getTestingRois(image);
					if(testing_roi_list!=null) {
						for (Roi roi:testing_roi_list) {
							predictionResultClassification.put(roi.getName(), getRoiPredictionForClassification(roi));							
						}
					}
				}
										
			//	System.out.println("the size of list of roi is in FM "+predictionResultClassification.size());								
			}
			
			//segmentation setting
			else {
				
				
				//get the current image
				ImagePlus currentImage = getCurrentImage();
				
				//segmented image instance
				ImagePlus classifiedImage;
				
				//classificationResult would have no of terms as number of pixels in particular image, 
				//expects createAllinstance would provide instances of all pixels of the particular image
				double[] classificationResult = learningManager
						.applyClassifier(featureMap.get(ProjectType.valueOf(projectInfo.getProjectType()).toString()).createAllInstance(image));
				
				//now classificationResult has predictions of all pixels of one particular image
				ImageProcessor classifiedSliceProcessor = new FloatProcessor(currentImage.getWidth(),
						currentImage.getHeight(), classificationResult);
				classifiedImage = new ImagePlus(image, classifiedSliceProcessor);
				classifiedImage.setCalibration(currentImage.getCalibration());
				IJ.save(classifiedImage, featurePath + image);
			}
												
		}
		
		if(ProjectType.valueOf(projectInfo.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
			return null;
		}
		return getClassifiedImage();
	}
	
	public String getProjectType() {
		return this.projectInfo.getProjectType();
	}

	@Override
	public Color getClassColor(String key) {
		return classes.get(key).getColor();
	}

	@Override
	public void updateColor(String key, Color color) {
		classes.get(key).setColor(color);
	}

	@Override
	public int getTotalSlice() {

		return this.images.size();
	}

	@Override
	public ImagePlus getCurrentImage() {
		if (sliceNum == 0) {
			return createImageIcon("no-image.jpg");
		}
		return new ImagePlus(projectString + this.images.get(sliceNum - 1));
	}

	private ImagePlus getImage(String image) {
		return new ImagePlus(projectString + image);
	}

	@Override
	public int getCurrentSlice() {
		return this.sliceNum;
	}

	@Override
	public ImagePlus getClassifiedImage() {
		return new ImagePlus(featurePath + this.images.get(sliceNum - 1));
	}

	@Override
	public ImagePlus getNextImage() {
		if (this.sliceNum < totalSlices) {
			this.sliceNum += 1;
		}
		//System.out.println("next slice"+sliceNum);
		return new ImagePlus(projectString + this.images.get(sliceNum - 1));
	}

	@Override
	public ImagePlus getPreviousImage() {
		if (this.sliceNum > 1) {
			this.sliceNum -= 1;
		}
		return new ImagePlus(projectString + this.images.get(sliceNum - 1));
	}

	private ImagePlus createImageIcon(String path) {
		java.net.URL imgURL = FeatureManagerNew.class.getResource(path);
		if (imgURL != null) {
			return new ImagePlus(imgURL.getPath());
		} else {
			return null;
		}
	}

	@Override
	public List<Color> getColors() {
		List<Color> colors = new ArrayList<>();
		for (ClassInfo classInfo : classes.values()) {

			colors.add(classInfo.getColor());
		}
		return colors;
	}

}
