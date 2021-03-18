package activeSegmentation.learning;

import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import weka.filters.unsupervised.attribute.PrincipalComponents;
import weka.core.Instances;
import weka.filters.Filter;

public class PCA implements IFeatureSelection {

private PrincipalComponents filter;
	
	private String selectionName="PCA";
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		//ASEvaluation asEvaluation=
		Instances trainingData= data.getDataset();
		trainingData.setClassIndex(trainingData.numAttributes()-1);
		filter = new PrincipalComponents();
		
		Instances filteredIns = null;
		// Evaluator
		
		// Assign evaluator to filter
		//filter.setEvaluator(evaluator);
		// Search strategy: best first (default values)
		
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

	@Override
	public String getName() {
		
		return this.selectionName;
	}

}
