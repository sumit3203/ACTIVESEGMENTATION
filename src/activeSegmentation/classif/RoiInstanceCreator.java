package activeSegmentation.classif;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.ASCommon;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeature;
import activeSegmentation.IFilterManager;
import activeSegmentation.learning.WekaDataSet;
import activeSegmentation.prj.ClassInfo;
import activeSegmentation.prj.ProjectInfo;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.util.ArrayUtil;
import ijaux.datatype.Pair;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 				
 *   
 * 
 * @author Mukesh Gupta, Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 *  Feature extraction at Class Level
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
public class RoiInstanceCreator implements IFeature {


	private Instances trainingData;
	private String featureName="classLevel";
	private ProjectInfo projectInfo;
	//private List<String> classLabels;
	private Map<String, double[]> instanceMap= new HashMap<String, double[]>();
	int classindex = 0;
	private int numberOfFeatures;
	private String projectString;
	private List<String> images;
	
	
	public RoiInstanceCreator(ProjectInfo projectInfo){
		this.projectInfo=projectInfo;
		//this.classLabels=new ArrayList<String>();
		this.projectString=this.projectInfo.getProjectDirectory().get(ASCommon.K_IMAGESDIR);
		this.images=new ArrayList<String>();
		loadImages(this.projectString);
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
	public void createTrainingInstance(Collection<ClassInfo> classInfos) {
		instanceMap.clear();
		//featureIndex.clear();
		Map<String, Set<String>> featureNames=projectInfo.getFeatureNames();
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		int featureSIndex=0;
		for(String key: featureNames.keySet()) {
			//featureIndex.put(key, featureSIndex);
			featureSIndex+=featureNames.get(key).size();
			for(String attribute: featureNames.get(key)) {
				attributes.add(new Attribute(attribute));
			}

		}
		//System.out.println(attributes.toString());
		numberOfFeatures=featureSIndex;
		Map<String,List<Pair<String,double[]>>> featureList=projectInfo.getFeatures();
		for(String featuresType: featureList.keySet()) {
		//	System.out.println("Feature Type:"+ featuresType);
			List<Pair<String,double[]>> features=featureList.get(featuresType);
			for(Pair<String,double[]> feature: features ) {
				if(!instanceMap.containsKey(feature.first)) {
					//System.out.println(Arrays.toString(feature.second));
					instanceMap.put(feature.first, feature.second);
				} else {
					double[] temp=instanceMap.get(feature.first);
					double[] current=feature.second;
					double[] newFeature=combine(temp, current);
					instanceMap.put(feature.first, newFeature);
				}
			}
			
		}

		attributes.add(new Attribute(ASCommon.CLASS, getCLassLabels(classInfos)));
		// create initial set of instances
		trainingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		// Set the index of the class attribute
		trainingData.setClassIndex(numberOfFeatures);	
		
		for(String image: images){
			int index=0;
			for(ClassInfo classInfo : classInfos){
				if(classInfo.getTrainingRois(image)!=null) {
					for(Roi roi:classInfo.getTrainingRois(image)) {
						double vector[]=instanceMap.get(roi.getName());
						double classIndex[]= {index};
						double[] newFeature=combine(vector, classIndex);
						//System.out.println(Arrays.toString(newFeature));
						trainingData.add(new DenseInstance(1.0,newFeature));
					}
					index++;
				}
			}
		}
     //System.out.println("TRaininf Data Size"+trainingData.size());
    // System.out.println(trainingData.toString());
	}

	
	public double[] combine(double[] a, double[] b){
        int length = a.length + b.length;
        double[] result = new double[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

	private List<String> getCLassLabels(Collection<ClassInfo>  classInfos) {

		List<String> labels= new ArrayList<String>();
		for(ClassInfo classInfo:classInfos) {
			labels.add(classInfo.getLabel());
		}
		//this.classLabels=labels;
		return labels;
	}
	@Override
	public String getFeatureName() {
		return featureName;
	}
 
	@Override
	public IDataSet getDataSet() {
		return new WekaDataSet(trainingData);
	}

	@Override
	public void setDataset(IDataSet trainingData) {
		this.trainingData= trainingData.getDataset();

	}

	/*
	 * (non-Javadoc)
	 * @see activeSegmentation.IFeature#createAllInstance(java.lang.String)
	 * TODO implement
	 */
	@Override
	public IDataSet createAllInstances(String image) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Instance createInstance(Roi roi) {
		//System.out.println("IN ROI INSTANCE Creator"+roi.getName());
		
		double vector[]=instanceMap.get(roi.getName());
		double classIndex[]= {0};
		double[] newFeature=combine(vector, classIndex);
		Instance instance=new DenseInstance(1.0,newFeature);
		instance.setDataset(trainingData);
		return instance;
	}

}
