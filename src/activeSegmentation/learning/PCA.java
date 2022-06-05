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

private PrincipalComponents filter = new PrincipalComponents();
	
	public PCA() {
	}
	
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);				
		// Apply filter
		try {
			filter.setInputFormat(data1);
			Instances filteredIns   = Filter.useFilter(data1, filter);
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return null;
	}
	
	// superfluous method?
	@Override
	public IDataSet filterTestData(IDataSet data){

		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		try {
			//filter.setInputFormat(data1);
			Instances filteredIns  = Filter.useFilter(data1, filter);
			return new WekaDataSet(filteredIns);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
