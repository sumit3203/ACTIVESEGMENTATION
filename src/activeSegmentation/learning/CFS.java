package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

//import java.util.Enumeration;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.learning.weka.WekaDataSet;
import ij.IJ;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Attribute;
//import weka.core.Attribute;
//import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

@AFilter(key="CFS", value="Correlation Feature Selection", type=FEATURE)
public class CFS implements IFeatureSelection {

	private AttributeSelection filter = new AttributeSelection();
	
	public CFS() {}
	
	/*
	 * 
	 */
	@Override
	public IDataSet selectFeatures(IDataSet data){
		
		Instances data1= data.getDataset();
		data1.setClassIndex(data1.numAttributes()-1);
		
		// Evaluator
		final CfsSubsetEval evaluator = new CfsSubsetEval();
		evaluator.setMissingSeparate(true);
		// Assign evaluator to filter
		filter.setEvaluator(evaluator);
		
		// Apply filter
		try {
			filter.setInputFormat(data1);
			BestFirst search1 = new BestFirst();
			filter.setEvaluator(evaluator);
			// Search strategy: best first (default values)
			filter.setSearch(search1);
			Instances filteredIns = Filter.useFilter(data1, filter);		
			filteredIns.deleteWithMissingClass();
			//System.out.println(filter.toString());
			//System.out.println(search1.globalInfo());
			IJ.log(search1.toString());
			System.out.println(evaluator.toString());
			
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


}
