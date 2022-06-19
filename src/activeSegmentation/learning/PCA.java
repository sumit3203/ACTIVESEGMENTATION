package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

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

@AFilter(key="PCA", value="Principal Component Analysis", type=FEATURE)
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
				IJ.log(attributes.nextElement().name());
			 }
				
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param instances
	 * @param evaluator
	 * @return
	 * @throws Exception
	 *
	private SortedSet<Entry<Attribute, Double>> selectedFeatures(Instances instances, PrincipalComponents evaluator){
		try {
			Map<Attribute, Double> scores=new HashMap<>();
			
			for (int i=0;i<instances.numAttributes(); i++) {
				Attribute t_attr=instances.attribute(i);
				
				if (! t_attr.name().equalsIgnoreCase("class")) {
					double infogain=evaluator.evaluateAttribute(i);
					scores.put(t_attr, infogain);
				}
			}
			
			 SortedSet<Entry<Attribute, Double>> sortedscores = IFeatureSelection.sortByVal(scores, -1);
			return sortedscores;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	*/
	
 
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

}
