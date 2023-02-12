package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import java.util.ArrayList;
import java.util.Enumeration;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
import ij.IJ;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
//import weka.filters.unsupervised.attribute.Normalize;
//import weka.filters.unsupervised.attribute.RemoveUseless;
//import weka.filters.unsupervised.attribute.Standardize;

@AFilter(key="PCA", value="Principal Component Analysis", type=FEATURE, help = "")
public class PCA implements IFeatureSelection {

  
	private AttributeSelection filter = new AttributeSelection();
	
	private PrincipalComponents pca = new PrincipalComponents();
	
	public PCA() {
	}
	
		  
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);				
		// Apply filter
		try {
			/*
			 Standardize norm = new Standardize();
			 norm.setInputFormat(data1);
 			 Instances normeddata1 = Standardize.useFilter(data1, filter);
			*/
			 final Ranker ranker = new Ranker();
			 
			 pca.initializeAndComputeMatrix(data1);
	
			 pca.buildEvaluator(data1);
 
			 pca.setVarianceCovered(0.9);
			 	 
			 filter.setInputFormat(data1);
		     filter.setEvaluator(pca);
		     filter.setSearch(ranker);
		     
	
		     Instances filteredIns = Filter.useFilter(data1, filter);
		    // IJ.log(ranker.toString());
		
		     Enumeration<Attribute> attributes=filteredIns.enumerateAttributes();
			 IJ.log("Selected features:");
			 while (attributes.hasMoreElements()) {
				 String fname= attributes.nextElement().name();
				 flist.add(fname);
				  IJ.log(fname);
				
			 }
				
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return null;
	}
	
	private ArrayList<String> flist=new ArrayList<>();
	
 
	@Override
	public IDataSet filterData(IDataSet data){

		final Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		try {
		    Instances filteredIns = Filter.useFilter(data1, filter); 	  
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String[] getFeatureList() {
		String[] str= {""};
		return flist.toArray(str);
	}

}
