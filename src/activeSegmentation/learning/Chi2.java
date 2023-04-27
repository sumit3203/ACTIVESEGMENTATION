package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import java.util.ArrayList;
import java.util.Enumeration;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
import ij.IJ;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.RemoveUseless;

@AFilter(key="Chi2", value="Chi 2 Feature Selection", type=FEATURE, help = "")
public class Chi2 implements IFeatureSelection {

	private AttributeSelection filter = new AttributeSelection();
		
	 
	
	/**
	 * 
	 */
	public Chi2() {}

	
	/*
	 * 
	 */
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		try {
		
		
			RemoveUseless ru = new RemoveUseless();
			ru.setInputFormat(data1);
			final Instances normeddata1 = Filter.useFilter(data1, ru);
		
			// Evaluator
			ChiSquaredAttributeEval evaluator = new ChiSquaredAttributeEval();
			evaluator.buildEvaluator(normeddata1);
	  
			// Assign evaluator to filter
			filter.setEvaluator(evaluator);
		 
			final Ranker ranker = new Ranker();
			ranker.setGenerateRanking(true);
			ranker.setThreshold(0.1);
			
			filter.setSearch(ranker);
			// Apply filter
			 
			filter.setInputFormat(data1);
			filter.setEvaluator(evaluator);
			filter.setSearch(ranker);
			Instances filteredIns = Filter.useFilter(data1, filter);		
			filteredIns.deleteWithMissingClass();
			/*
			SortedSet<Entry<Attribute, Double>> sortedscores = selectedFeatures(data1, evaluator);
			IJ.log("Selected features:");
			for (Entry<Attribute, Double> c:sortedscores) {
				// System.out.println(c.getKey().name()+ " "+ c.getValue());
				 IJ.log(c.getKey().name()+ " "+ c.getValue());
			 }
			*/
			
			Enumeration<Attribute> attributes=filteredIns.enumerateAttributes();
 
			IJ.log("Selected features:");
			int c=0;
			while (attributes.hasMoreElements()) {
				String fname= attributes.nextElement().name();
				flist.add(fname);
				IJ.log(fname);
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
	
	private ArrayList<String> flist=new ArrayList<>();
	

	
	@Override
	public IDataSet filterData(IDataSet data){

		final Instances testData= data.getDataset();
		testData.setClassIndex(testData.numAttributes()-1);
		try {
			Instances filteredIns  = Filter.useFilter(testData, filter);
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
