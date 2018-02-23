package activeSegmentation.featureSelection;



import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.WekaDataSet;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

public class GainRatio implements IFeatureSelection {

	private AttributeSelection filter;
	
	private String selectionName="INFO";
	public IDataSet selectFeatures(IDataSet data){
		
		//ASEvaluation asEvaluation=
		Instances trainingData= data.getDataset();
		trainingData.setClassIndex(trainingData.numAttributes()-1);
		 filter = new AttributeSelection();
		Instances filteredIns = null;
		// Evaluator
		final GainRatioAttributeEval evaluator = new GainRatioAttributeEval();
		//evaluator.setMissingSeparate(true);
		// Assign evaluator to filter
		filter.setEvaluator(evaluator);
		// Search strategy: best first (default values)
		final Ranker search = new Ranker();
		search.setNumToSelect(33);
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

	@Override
	public String getName() {
		
		return this.selectionName;
	}

}
