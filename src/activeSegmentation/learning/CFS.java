package activeSegmentation.learning;



import static activeSegmentation.FilterType.FEATURE;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

@AFilter(key="CFS", value="Correlation Feature Selection", type=FEATURE)
public class CFS implements IFeatureSelection {

	private AttributeSelection filter;
	
	public CFS( ) {
		
	}
	
	/*
	 * 
	 */
	@Override
	public IDataSet selectFeatures(IDataSet data){
		

		Instances trainingData= data.getDataset();
		trainingData.setClassIndex(trainingData.numAttributes()-1);
		filter = new AttributeSelection();

		// Evaluator
		final CfsSubsetEval evaluator = new CfsSubsetEval();
		evaluator.setMissingSeparate(true);
		// Assign evaluator to filter
		filter.setEvaluator(evaluator);
		// Search strategy: best first (default values)
		final BestFirst search = new BestFirst();
		filter.setSearch(search);
		// Apply filter
		try {
			filter.setInputFormat(trainingData);
			 BestFirst search1 = new BestFirst();

			filter.setEvaluator(evaluator);
			filter.setSearch(search1);
			Instances filteredIns = Filter.useFilter(trainingData, filter);
			
			filteredIns.deleteWithMissingClass();
	
			// int numattr = filter.numberAttributesSelected();
			//filter.SelectAttributes(trainingData);
			
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public IDataSet applyOnTestData(IDataSet data){

		Instances testData= data.getDataset();
		testData.setClassIndex(testData.numAttributes()-1);
		try {
			Instances filteredIns  = Filter.useFilter(testData, filter);
			new WekaDataSet(filteredIns);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
