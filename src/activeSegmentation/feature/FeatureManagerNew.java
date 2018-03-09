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

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
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
public class FeatureManagerNew implements IFeatureManagerNew {



	private IProjectManager projectManager;
	private ProjectInfo projectInfo;
	private Random rand = new Random();
	private String projectString;
	private int sliceNum,totalSlices;
	private List<String> images;
	private Map<String,IFeature> featureMap= new HashMap<String, IFeature>();
	private static RoiManager roiman= new RoiManager();
	private Map<String,ClassInfo> classes= new HashMap<String, ClassInfo>();
	public FeatureManagerNew(IProjectManager projectManager)
	{
		this.projectManager= projectManager;
		this.projectInfo=this.projectManager.getMetaInfo();
		this.images= new ArrayList<String>();
		this.projectString=this.projectInfo.getProjectPath()+"/"+this.projectInfo.getProjectName()+"/"+ "Training/images/";
		this.totalSlices=loadImages(this.projectString);
		if(this.totalSlices>0){
			this.sliceNum=1;
		}
		for(int i=1; i<=projectInfo.getClasses();i++ ){
			addClass();
		}
		roiman.hide();
	}


	private int loadImages(String directory){
		this.images.clear();
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isFile()) {
				this.images.add(file.getName());
			}
		}
		return this.images.size();
	}

	@Override
	public void addExample(String key, Roi roi, String type) {
		String imageKey=this.images.get(sliceNum-1);
		if(LearningType.valueOf(type).equals(LearningType.TESTING)){
			classes.get(key).addTestingRois(imageKey, roi);
		}else{
			classes.get(key).addTrainingRois(imageKey, roi);
		}
		roiman.addRoi(roi);
	}

	@Override
	public void addExampleList(String classNum, List<Roi> roiList, String type) {
		for(Roi roi: roiList){
			if(processibleRoi(roi)){
				addExample(classNum, roi, type);
			}
		}
	}
	
	private boolean processibleRoi(Roi roi) {
		boolean ret=(roi!=null && !(roi.getType()==Roi.LINE || 
				roi.getType()==Roi.POLYLINE ||
				roi.getType()==Roi.ANGLE ||
				roi.getType()==Roi.FREELINE ||
				roi.getType()==Roi.POINT
				));
		return ret;
	}

	@Override
	public void deleteExample(String key, int index, String type) {
		String imageKey=this.images.get(sliceNum-1);
		if(LearningType.valueOf(type).equals(LearningType.TESTING)){
			classes.get(key).getTestingRois().get(imageKey).remove(index);
		}
		else{
		    classes.get(key).getTrainingRois().get(imageKey).remove(index);
		}

	}


	@Override
	public List<Roi> getExamples(String key, String type) {
		String imageKey=this.images.get(sliceNum-1);
		if(LearningType.valueOf(type).equals(LearningType.TESTING)){
			return classes.get(key).getTestingRois().get(imageKey);
		}
		else{
			return classes.get(key).getTrainingRois().get(imageKey);
		}

	}
	
	@Override
	public Roi getRoi(String key, int index, String type) {
		String imageKey=this.images.get(sliceNum-1);
		if(LearningType.valueOf(type).equals(LearningType.TESTING)){
			return classes.get(key).getTestingRois().get(imageKey).get(index);
		}
		else{
			return classes.get(key).getTrainingRois().get(imageKey).get(index);
		}
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
	public int getRoiListSize(String key,String learningType) {
		String imageKey=this.images.get(sliceNum-1);
		if(LearningType.valueOf(learningType).equals(LearningType.TESTING)){
              return classes.get(key).getTestingRoiSize(imageKey);
		}else{
			 return classes.get(key).getTrainingRoiSize(imageKey);	
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
		String key=UUID.randomUUID().toString();
		if(!classes.containsKey(key)){
			Map<String,List<Roi>> trainingRois= new HashMap<String, List<Roi>>();	
			Map<String,List<Roi>> testingRois= new HashMap<String, List<Roi>>();	
			ClassInfo classInfo= new ClassInfo(key, "label"+classes.size(),getColor(),trainingRois, testingRois);
			classes.put(key, classInfo);
		}
	}

	private Color getColor(){
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color randomColor = new Color(r, g, b);
		return randomColor;
	}
	@Override
	public void deleteClass(String key) {
		classes.remove(key);
	}


	@Override
	public void setFeatureMetadata() {


	}

	@Override
	public void saveFeatureMetadata() {
		

	}

	@Override
	public IDataSet extractFeatures(String featureType) {
		
		return null;
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
	public boolean saveExamples(String filename, String  classKey,String type) {
		System.out.println(classKey+type);
		List<Roi> rois= getExamples(classKey, type);
		System.out.println(rois.size());
		DataOutputStream out = null;
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
			out = new DataOutputStream(new BufferedOutputStream(zos));
			RoiEncoder re = new RoiEncoder(out);
			for (Roi roi:rois) {
                System.out.println(roi.getName());
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
	public void uploadExamples(String filename,String classKey,String type) {
		
		Hashtable rois = new Hashtable();
		ZipInputStream in = null; 
		List<Roi> roiList= new ArrayList<Roi>();
		ByteArrayOutputStream out = null; 
		int nRois = 0; 
		try { 
			in = new ZipInputStream(new FileInputStream(filename)); 
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
		
		addExampleList(classKey, roiList, type);
		
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

	@Override
	public ImagePlus compute(String featureType) {
		
		return null;
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
		if(sliceNum==0){
			createImageIcon("no-image.jpg");
		}
		return new ImagePlus(projectString+this.images.get(sliceNum-1));
	}


	@Override
	public int getCurrentSlice() {
		
		return this.sliceNum;
	}


	@Override
	public ImagePlus getNextImage() {
		if(this.sliceNum<totalSlices){
			this.sliceNum+=1;
		}
		return new ImagePlus(projectString+this.images.get(sliceNum-1));
	}


	@Override
	public ImagePlus getPreviousImage() {
		if(this.sliceNum>1){
			this.sliceNum-=1;
		}
		return new ImagePlus(projectString+this.images.get(sliceNum-1));
	}

	private  ImagePlus  createImageIcon(String path) {
		java.net.URL imgURL = FeatureManagerNew.class.getResource(path);
		if (imgURL != null) {
			return new ImagePlus(imgURL.getPath());
		} else {            
			return null;
		}
	}


	

}
