package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import java.util.Arrays;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
 
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.Standardize;

@AFilter(key="PCA", value="Principal Component Analysis", type=FEATURE)
public class PCA implements IFeatureSelection {

  

private AttributeSelection filter = new AttributeSelection();

PrincipalComponents pca = new PrincipalComponents();
	
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
			 Ranker ranker = new Ranker();
			 
			 pca.initializeAndComputeMatrix(data1);
	
			// pca.setMaximumAttributeNames(25);
			  pca.buildEvaluator(data1);
 
			 pca.setVarianceCovered(0.9);
			 	 
			 filter.setInputFormat(data1);
		     filter.setEvaluator(pca);
		     filter.setSearch(ranker);
		     
			  
		     Instances filteredIns = Filter.useFilter(data1, filter);
			
			 
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return null;
	}
	
 
	@Override
	public IDataSet filterData(IDataSet data){

		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		try {
 
			  
		     Instances filteredIns = Filter.useFilter(data1, filter);
			  
		  
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
