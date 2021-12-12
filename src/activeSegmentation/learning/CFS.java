package activeSegmentation.learning;



import static activeSegmentation.FilterType.FEATURE;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

@AFilter(key="CFS", value="Correlation Feature Selection", type=FEATURE)
public class CFS implements IFeatureSelection {

	private AttributeSelection filter;
	
	/*
	 * 
	 */
	@Override
	public IDataSet selectFeatures(IDataSet data){
		

		Instances trainingData= data.getDataset();
		trainingData.setClassIndex(trainingData.numAttributes()-1);
		 filter = new AttributeSelection();
		Instances filteredIns = null;
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
			filteredIns = Filter.useFilter(trainingData, filter);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return new WekaDataSet(filteredIns);
	}
	
	@Override
	public IDataSet applyOnTestData(IDataSet data){
		Instances filteredIns = null;
		Instances testData= data.getDataset();
		testData.setClassIndex(testData.numAttributes()-1);
		try {
			filteredIns = Filter.useFilter(testData, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new WekaDataSet(filteredIns);
	}


}
