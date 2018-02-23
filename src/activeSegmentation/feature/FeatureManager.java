package activeSegmentation.feature;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import activeSegmentation.Common;
import activeSegmentation.IProjectManager;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureManager;
import activeSegmentation.ILearningManager;
import activeSegmentation.IFeature;
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
 * @contents
 * Feature Manager to store , update and delete Samples , It also consist of code to load from metafile
 * 
 * 
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
public class FeatureManager implements IFeatureManager {

	/** array of lists of Rois for each slice (vector index) 
	 * and each class (arraylist index) of the training image */
	private List<Vector<ArrayList<Roi>>> examples;

	/**
	 * A list contains classes and each class contain another list of image index of the training image
	 */
	private List<ArrayList<Integer>> imageType;

	/**
	 * A list contains classes and each class contain another list of image index of the testing image
	 */
	private List<ArrayList<Integer>> imageTestType;

	private IProjectManager projectManager;

	private ProjectInfo projectInfo;

	private Map<String,IFeature> featureMap= new HashMap<String, IFeature>();

	/** maximum number of classes (labels) allowed */
	/** names of the current classes */
	private Map<Integer,String> classLabels = new HashMap<Integer, String>();
	
	private static RoiManager roiman= new RoiManager();

	/** current number of classes */
	private int numOfClasses = 0;
	ILearningManager  learningManager;
	private ImagePlus originalImage;
	private int stackSize=0;

	public FeatureManager(int stackSize,IProjectManager projectManager)
	{
		this.stackSize=stackSize;
		this.examples= new ArrayList<Vector<ArrayList<Roi>>>();
		this.imageType = new ArrayList<ArrayList<Integer>>();
		this.imageTestType = new ArrayList<ArrayList<Integer>>();
		this.projectManager= projectManager;	
		this.projectInfo=projectManager.getMetaInfo();
		this.originalImage= IJ.openImage(projectInfo.getTrainingStack());
		// update list of examples
		for(int i=0; i < stackSize; i++)
		{
			examples.add(new Vector<ArrayList<Roi>>());			
			imageType.add(new ArrayList<Integer>());
			imageTestType.add(new ArrayList<Integer>());
		}

		for(int i=1; i<=projectInfo.getClasses();i++ ){
			addClass();
		}
		featureMap.put("pixelLevel", new PixelInstanceCreator(projectInfo));
		learningManager= new ClassifierManager(projectManager);

	}

	public void addExample(int classNum, Roi roi, int n) 
	{
		System.out.println(roi);
		System.out.println("ADD EXAMLE");
		examples.get(n-1).get(classNum).add(roi);
		roiman.addRoi(roi);
	}
	
	public void addImageType(int classNum, int nSlice) 
	{
		for(int i=0;i<imageType.size();i++){
			if(imageType.get(i).contains(nSlice))
				imageType.get(i).remove(imageType.get(i).indexOf(nSlice));
			if(imageTestType.get(i).contains(nSlice))
				imageTestType.get(i).remove(imageTestType.get(i).indexOf(nSlice));
		}
		imageType.get(classNum).add(nSlice);
	}

	public void addTestImageType(int classNum, int nSlice){
		for(int i=0;i<imageTestType.size();i++){
			if(imageType.get(i).contains(nSlice))
				imageType.get(i).remove(imageType.get(i).indexOf(nSlice));
			if(imageTestType.get(i).contains(nSlice))
				imageTestType.get(i).remove(imageTestType.get(i).indexOf(nSlice));
		}
		imageTestType.get(classNum).add(nSlice);
	}

	@Override
	public void addExampleList(int classNum, List<Roi> roiList, int n) {
		// TODO Auto-generated method stub
		for(Roi roi: roiList){
			if(processibleRoi(roi)){
				addExample(classNum, roi, n);
			}
		}
	}

	/**
	 * Return the list of examples for a certain class.
	 * 
	 * @param classNum the number of the examples' class
	 * @param n the slice number
	 */
	public List<Roi> getExamples(int classNum, int n) 
	{
		System.out.println("size"+examples.size());
		System.out.println("class Num"+ classNum+ " slice No"+ n);
		return examples.get(n-1).get(classNum);
	}
	
	public ArrayList<Integer> getImageTestType(){
		ArrayList<Integer> imageindex = new ArrayList<Integer>();
		
		for(ArrayList<Integer> arr : imageTestType){
			for(Integer i:arr)
				imageindex.add(i);
		}
		return imageindex;		
	}

	@Override
	public int  getclassKey(String classNum){
		for (Map.Entry<Integer,String> e : classLabels.entrySet()) {
			Integer key = e.getKey();
			Object value2 = e.getValue();
			if ((value2.toString()).equalsIgnoreCase(classNum))
			{
				return key;
			}
		} 
		return 0;
	}

	/**
	 * Remove an example list from a class and specific slice
	 * 
	 * @param classNum the number of the examples' class
	 * @param nSlice the slice number
	 * @param index the index of the example list to remove
	 */
	public void deleteExample(int classNum, int nSlice, int index)
	{
		getExamples(classNum, nSlice).remove(index);
	}
	
	/**
	 * Remove an slice from dataset. 
	 * 
	 * @param sliceNum the number of the examples' class
	 */
	public void deleteImageType(int classId, int sliceNum)
	{
		if(imageType.get(classId).indexOf(sliceNum)!=-1)
			imageType.get(classId).remove(imageType.get(classId).indexOf(sliceNum));
		else
			imageTestType.get(classId).remove(imageTestType.get(classId).indexOf(sliceNum));
	}

	/**
	 * Get the current class labels
	 * @return array containing all the class labels
	 */
	@Override
	public List<String> getClassLabels() 
	{
		return new ArrayList<String>(classLabels.values());
	}

	@Override
	public String getClassLabel(int index) {
		// TODO Auto-generated method stub
		return classLabels.get(index);
	}

	/**
	 * Set the name of a class.
	 * 
	 * @param classNum class index
	 * @param label new name for the class
	 */
	@Override
	public void setClassLabel(int classNum, String label) 
	{
		//classLabels.add(classNum-1, label);
		classLabels.put(classNum, label);
	}

	/**
	 * Set the current number of classes. Should not be used to create new
	 * classes. Use <link>addClass<\link> instead.
	 *
	 * @param numOfClasses the new number of classes
	 */
	@Override
	public void setNumOfClasses(int numOfClasses) {
		this.numOfClasses = numOfClasses;
	}

	/**
	 * Get the current number of classes.
	 *
	 * @return the current number of classes
	 */
	@Override
	public int getNumOfClasses() 
	{
		return numOfClasses;
	}

	/**
	 * Add new segmentation class.
	 */
	public void addClass()
	{

		for(int i=1; i <= stackSize; i++)
			examples.get(i-1).add(new ArrayList<Roi>());

		numOfClasses ++;
		classLabels.put(numOfClasses ,new String(Common.CLASS + (numOfClasses)));
		// increase number of available classes

	}

	private boolean processibleRoi(Roi roi) {
		boolean ret=(roi!=null && !(roi.getType()==Roi.LINE || 
				roi.getType()==Roi.POLYLINE ||
				roi.getType()==Roi.ANGLE ||
				roi.getType()==Roi.FREELINE ||
				roi.getType()==Roi.POINT
				)
				);

		return ret;

	}

	public int getStackSize() {
		return stackSize;
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	@Override
	public void setFeatureMetadata(){
		projectInfo= projectManager.getMetaInfo();
		Map<String,String> keywordList= projectInfo.getKeywordList();
		if(keywordList!=null){
			for(String key:keywordList.keySet()){
				Integer classId=Integer.parseInt(key);	
				if(numOfClasses>=classId){
					setClassLabel(classId, keywordList.get(key));	
					System.out.println("classId"+classId);
					System.out.println("No.ofClasses "+numOfClasses);
				}
				else{
					addClass();
					System.out.println("classId 1"+classId);
					System.out.println("No.ofClasses "+numOfClasses);
					setClassLabel(classId, keywordList.get(key));	
				}
			}
		}

		for(FeatureInfo featureInfo : projectInfo.getFeatureList() ){
			int classNum=featureInfo.getClassLabel();
			System.out.println(projectInfo.getPath()+featureInfo.getZipFile());
			List<Roi> classRoiList=openZip(projectInfo.getPath()+featureInfo.getZipFile());
			System.out.println(classRoiList.size());
			for( String s: featureInfo.getSliceList().keySet()){
				Integer sliceNum= Integer.parseInt(s.substring(s.length()-1));
				System.out.println("slicenum-"+sliceNum);
				List<String> sliceRois= featureInfo.getSliceList().get(s);	
				addExampleList(classNum, getRois(classRoiList, sliceRois), sliceNum);
			}
		}

		System.out.println(examples.size());
		System.out.println(projectInfo.toString());

	}

	private List<Roi> getRois(List<Roi> classRoiList, List<String> roiNames){
		List<Roi> roiList= new ArrayList<Roi>();
		for(String name: roiNames){
			System.out.println(name);
			for(Roi roi: classRoiList){
				System.out.println(roi.getName());
				if(roi.getName().equalsIgnoreCase(name)){
					roiList.add(roi);
				}
			}
		}

		return roiList;
	}

	@Override
	public void saveFeatureMetadata(){
		projectInfo= projectManager.getMetaInfo();
		projectInfo.resetFeatureInfo();

		Map<String, String> keywordList = new HashMap<String, String>();
		for(Integer key:classLabels.keySet()){
			keywordList.put(key.toString(), classLabels.get(key));

		}
		projectInfo.setKeywordList(keywordList);		
		for(int classIndex = 0; classIndex <
				getNumOfClasses(); classIndex++)
		{
			FeatureInfo featureInfo= new FeatureInfo();
			List<Roi>  classRois= new ArrayList<Roi>();
			featureInfo.setClassLabel(classIndex);

			for(int sliceNum = 1; sliceNum <= 
					stackSize; sliceNum ++){
				List<Roi> rois=getExamples(classIndex, sliceNum);
				if(rois!=null & rois.size()>0){
					classRois.addAll(rois);
					List<String> roiArr=new ArrayList<String>();
					for(Roi roi: rois){
						roiArr.add(roi.getName());
					}	
					featureInfo.addSlice(Common.SLICE+sliceNum, roiArr);
				}

			}

			String fileName=Common.ROISET+classIndex+Common.FORMAT;
			if(classRois!=null & classRois.size()>0){
				System.out.println("examples"+projectInfo.getPath());
				saveExamples(projectInfo.getPath()+fileName,classRois );
				featureInfo.setZipFile(fileName);
			}

			projectInfo.addFeature(featureInfo);
		}			

		System.out.println("IN");
		System.out.println(projectInfo.toString());
		projectManager.writeMetaInfo(projectInfo);

	}

	@Override
	public IDataSet extractFeatures(String featureType){
		
		if(featureType.equals("classLevel"))
		{
			featureMap.get(featureType).createTrainingInstance(new ArrayList<String>(classLabels.values()),
					imageType.size(), imageType);
			
		}
		else {
			featureMap.get(featureType).createTrainingInstance(new ArrayList<String>(classLabels.values()),
					numOfClasses, examples);
		}
		IDataSet dataset=featureMap.get(featureType).getDataSet();
		projectManager.setData(dataset);
		System.out.println("NUMBER OF INSTANCE "+dataset.toString());
		return dataset;

	}
	

	@Override
	public List<IDataSet> extractAll(String featureType){
		List<IDataSet> dataset = null;
		if(featureType.equals("pixelLevel"))
			dataset= featureMap.get(featureType).createAllInstance(new ArrayList<String>(classLabels.values()),numOfClasses);
		else
			dataset = featureMap.get(featureType).createAllInstance(new ArrayList<String>(classLabels.values()),numOfClasses, imageTestType);
		return dataset;
	}

	@Override
	public Set<String> getFeatures(){
		return featureMap.keySet();
	}

	@Override
	public void addFeatures(IFeature feature){
		featureMap.put(feature.getFeatureName(), feature);
	}

	@Override
	public int getSize(int i, int currentSlice) {
		// TODO Auto-generated method stub
		return getExamples(i, currentSlice).size();
	}

	@Override
	public ArrayList<Integer> getDataImageTypeId(int ClassNum) {
		// TODO Auto-generated method stub
		
		if(imageType.size()==0)
			return null;
		
		return imageType.get(ClassNum);
	}

	@Override
	public ArrayList<Integer> getDataImageTestTypeId(int ClassNum) {
		// TODO Auto-generated method stub
		return imageTestType.get(ClassNum);
	}

	@Override
	public int getClassIdofCurrentSlicetraining(int currentSlice) {
		// TODO Auto-generated method stub
		for(int i=0;i<imageType.size();i++){
			if(imageType.get(i).contains(currentSlice))
				return i;
		}
		return -1;
	}

	@Override
	public int getClassIdofCurrentSlicetesting(int currentSlice) {
		// TODO Auto-generated method stub
		for(int i=0;i<imageTestType.size();i++){
			if(imageTestType.get(i).contains(currentSlice))
				return i;
		}
		return -1;
	}
	
	@Override
	public boolean saveExamples(String filename, List<Roi> rois) {

		DataOutputStream out = null;
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
			out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (Roi roi:rois) {

				zos.putNextEntry(new ZipEntry(roi.getName()+".roi"));
				re.write(roi);
				out.flush();
			}
			out.close();
		} catch (IOException e) {

			return false;
		} finally {
			if (out!=null)
				try {out.close();} catch (IOException e) {}
		}

		return true;
	}
	
	@Override
	public List<Roi> openZip(String path) {
		// TODO Auto-generated method stub

		return openZip1(path);

	}

	private List<Roi> openZip1(String path) { 
		Hashtable rois = new Hashtable();
		ZipInputStream in = null; 
		List<Roi> roiList= new ArrayList<Roi>();
		ByteArrayOutputStream out = null; 
		int nRois = 0; 
		try { 
			in = new ZipInputStream(new FileInputStream(path)); 
			byte[] buf = new byte[1024]; 
			int len; 
			ZipEntry entry = in.getNextEntry(); 
			while (entry!=null) { 
				String name = entry.getName();
				if (name.endsWith(".roi")) { 
					out = new ByteArrayOutputStream(); 
					while ((len = in.read(buf)) > 0) 
						out.write(buf, 0, len); 
					out.close(); 
					byte[] bytes = out.toByteArray(); 
					RoiDecoder rd = new RoiDecoder(bytes, name); 
					Roi roi = rd.getRoi(); 
					if (roi!=null) { 
						name = name.substring(0, name.length()-4); 
						name = getUniqueName(name,rois);  
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
			if (in!=null)
				try {in.close();} catch (IOException e) {}
			if (out!=null)
				try {out.close();} catch (IOException e) {}
		}
		if(nRois==0)
			System.out.println("ERROR OCCURED");

		return roiList;
	} 

	private String getUniqueName(String name,Hashtable rois) {
		String name2 = name;
		int n = 1;
		Roi roi2 = (Roi)rois.get(name2);
		while (roi2!=null) {
			roi2 = (Roi)rois.get(name2);
			if (roi2!=null) {
				int lastDash = name2.lastIndexOf("-");
				if (lastDash!=-1 && name2.length()-lastDash<5)
					name2 = name2.substring(0, lastDash);
				name2 = name2+"-"+n;
				n++;
			}
			roi2 = (Roi)rois.get(name2);
		}
		return name2;
	}
	
	public ImagePlus compute(String featureType){
		extractFeatures(featureType);
		learningManager.trainClassifier();
		List<double[]> classificationResult=learningManager.applyClassifier(extractAll(featureType));
		
		ImageStack classStack = new ImageStack(originalImage.getWidth(), originalImage.getHeight());
		int i=1;
		for (double[] result: classificationResult)
		{
			ImageProcessor classifiedSliceProcessor = new FloatProcessor(originalImage.getWidth(),
					originalImage.getHeight(), result);				
			classStack.addSlice(originalImage.getStack().getSliceLabel(i), classifiedSliceProcessor);
			i++;
		}
		ImagePlus classifiedImage= new ImagePlus("Classified Image", classStack);
		classifiedImage.setCalibration(originalImage.getCalibration());
		classifiedImage.show();
		return classifiedImage;
	}
}
