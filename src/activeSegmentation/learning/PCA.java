package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import weka.filters.unsupervised.attribute.PrincipalComponents;
import weka.core.Instances;
import weka.filters.Filter;

@AFilter(key="PCA", value="Principal Component Analysis", type=FEATURE)
public class PCA implements IFeatureSelection {

private PrincipalComponents filter;
	
	
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances trainingData= data.getDataset();
		trainingData.setClassIndex(trainingData.numAttributes()-1);
		filter = new PrincipalComponents();
		
		Instances filteredIns = null;
		
		
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
