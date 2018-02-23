package activeSegmentation.feature;

import java.util.ArrayList;
import java.util.List;

import activeSegmentation.Common;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeature;
import activeSegmentation.IFilterManager;
import activeSegmentation.learning.WekaDataSet;
import ij.ImagePlus;
import weka.core.Attribute;
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

	private IFilterManager filterManager;	
	private Instances trainingData;
	private String featureName="classLevel";
	int classindex = 0;
	public RoiInstanceCreator(IFilterManager filterManager, ImagePlus originalImage){
		this.filterManager= filterManager;
	}
	
	@Override
	public void createTrainingInstance(List<String> classLabels, int classes, List<?> features) {
		// TODO Auto-generated method stub

		@SuppressWarnings("unchecked")
		List<ArrayList<Integer>> imageType = (List<ArrayList<Integer>>) features;
		
		ArrayList<Attribute> attributes = createFeatureHeader();
		attributes.add(new Attribute(Common.CLASS, classLabels));
				
		//create initial set of instances
		trainingData =  new Instances(Common.INSTANCE_NAME, attributes, 1 );
					
		//Set the index of the class attribute
		trainingData.setClassIndex(classindex);
		for(int classIndex = 0; classIndex < classes; classIndex++)
		{
			for(int i=0; i<imageType.get(classIndex).size();i++){
				try{
			//		trainingData.add(filterManager.createInstance(featureName, classIndex, imageType.get(classIndex).get(i)));
				}catch(Exception e){
					e.printStackTrace();
				}
			}	
		}
		
	}
	
	@Override
	public String getFeatureName() {
		// TODO Auto-generated method stub
		return featureName;
	}

	private  ArrayList<Attribute> createFeatureHeader(){
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		int degree = filterManager.getNumOfFeatures(featureName);
		int k=0;
		while(k<=degree){
			for(int l=0;l<=k;l++){	
				if((k-l)%2==0){
				     attributes.add(new Attribute("Z"+k+","+l));
				     ++classindex;
				     if(l!=0){
				    	 attributes.add(new Attribute("Z"+k+",-"+l));
				    	 ++classindex;
				     }	 
				}
			}
			k++;
		}
		return attributes;
	}
	
	
	@Override
	public IDataSet getDataSet() {
		// TODO Auto-generated method stub
		return new WekaDataSet(trainingData);
	}

	@Override
	public void setDataset(IDataSet trainingData) {
		// TODO Auto-generated method stub
		this.trainingData= trainingData.getDataset();

	}

	@Override
	public List<IDataSet> createAllInstance(List<String> classLabels, int classes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IDataSet> createAllInstance(List<String> classLabels, int classes, List<ArrayList<Integer>> testimageindex) {
		// TODO Auto-generated method stub
		
		List<IDataSet> dataSets= new ArrayList<IDataSet>();
		for(int i=0; i<testimageindex.size();i++){
			for(int j=0;j<testimageindex.get(i).size();j++){
				Instances testingData;
				classindex=0;
				ArrayList<Attribute> attributes = createFeatureHeader();
				attributes.add(new Attribute(Common.CLASS,classLabels));
				testingData =  new Instances(Common.INSTANCE_NAME, attributes, 1 );
				// Set the index of the class attribute
				testingData.setClassIndex(classindex);
				//testingData.add(filterManager.createInstance(featureName, i, testimageindex.get(i).get(j)));
				dataSets.add(new WekaDataSet(testingData));
			}
		}
		
		return dataSets;        
	}

}
