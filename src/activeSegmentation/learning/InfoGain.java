package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
import ij.IJ;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.RemoveUseless;

@AFilter(key="InfoGain", value="InfoGain Feature Selection", type=FEATURE)
public class InfoGain implements IFeatureSelection {

	private AttributeSelection filter = new AttributeSelection();
	private boolean debug=false;
		
	/* code based on
	 * https://stackoverflow.com/questions/18744264/weka-filter-removeuseless-issue/18744867#18744867
	 */
	
	/**
	 * 
	 */
	public InfoGain() {}

	
	/*
	 * 
	 */
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		try {
			Normalize norm = new Normalize();
			norm.setInputFormat(data1);
		
			RemoveUseless ru = new RemoveUseless();
			ru.setInputFormat(data1);
			Instances normeddata1 = Filter.useFilter(data1, ru);
			
		
			// Evaluator
			InfoGainAttributeEval evaluator = new InfoGainAttributeEval();
			evaluator.buildEvaluator(normeddata1);
	  
			// Assign evaluator to filter
			filter.setEvaluator(evaluator);
		 
			Ranker ranker = new Ranker();
			ranker.setGenerateRanking(true);
			ranker.setThreshold(0.1);
			filter.setSearch(ranker);
			
			// Apply filter
			filter.setInputFormat(data1);
			//BestFirst search1 = new BestFirst();
			filter.setEvaluator(evaluator);
			filter.setSearch(ranker);
			Instances filteredIns = Filter.useFilter(data1, filter);		
			filteredIns.deleteWithMissingClass();
			IJ.log(ranker.toString());
			
			if  (debug) {
				SortedSet<Entry<Attribute, Double>> sortedscores = selectedFeatures(normeddata1, evaluator);
				IJ.log("Ranking of features:");
				for (Entry<Attribute, Double> c:sortedscores) {
					// System.out.println(c.getKey().name()+ " "+ c.getValue());
					 IJ.log(c.getKey().name()+ " "+ c.getValue());
				}
			}
			 
			Enumeration<Attribute> attributes=filteredIns.enumerateAttributes();
			IJ.log("Selected features:");
			int c=0;
			while (attributes.hasMoreElements()) {
				IJ.log(attributes.nextElement().name());
				c++;
			}
			IJ.log("Selected "+c);
			 
			evaluator.clean();
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
	 */
	private SortedSet<Entry<Attribute, Double>> selectedFeatures(Instances instances, InfoGainAttributeEval evaluator){
		try {
			Map<Attribute, Double> scores=new HashMap<>();
			
			for (int i=0;i<instances.numAttributes(); i++) {
				Attribute t_attr=instances.attribute(i);
				double infogain=evaluator.evaluateAttribute(i);
				if (! t_attr.name().equalsIgnoreCase("class"))
					scores.put(t_attr, infogain);
			}
			
			 SortedSet<Entry<Attribute, Double>> sortedscores = IFeatureSelection.sortByVal(scores, -1);
			return sortedscores;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	@Override
	public IDataSet filterData(IDataSet data){

		Instances testData= data.getDataset();
		testData.setClassIndex(testData.numAttributes()-1);
		try {
			Instances filteredIns  = Filter.useFilter(testData, filter);
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
