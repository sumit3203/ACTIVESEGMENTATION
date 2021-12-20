package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
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
				
		// Apply filter
		try {
			filter.setInputFormat(trainingData);
			Instances filteredIns   = Filter.useFilter(trainingData, filter);
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	// superfluous method?
	@Override
	public IDataSet applyOnTestData(IDataSet data){

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
