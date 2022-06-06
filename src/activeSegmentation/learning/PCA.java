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

@AFilter(key="PCA", value="Principal Component Analysis", type=FEATURE)
public class PCA implements IFeatureSelection {

private PrincipalComponents pca = new PrincipalComponents();

private AttributeSelection filter = new AttributeSelection();
	
	public PCA() {
	}
	
	 

	 
		  
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);				
		// Apply filter
		try {
			//filter.setInputFormat(data1);
			//Instances filteredIns   = Filter.useFilter(data1, filter);
			
			 Ranker ranker = new Ranker();
			 
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
	public IDataSet filterTestData(IDataSet data){

		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		try {
			//filter.setInputFormat(data1);
			//Instances filteredIns  = Filter.useFilter(data1, filter);
			Ranker ranker = new Ranker();			 
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

}
