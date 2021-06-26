package activeSegmentation.moment;

import static activeSegmentation.FilterType.CLASSIF;

import java.util.HashMap;

import java.util.Map;
import java.util.Set;

import activeSegmentation.AFilter;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;


@AFilter(key="imagej_features", value="IJ ROI statistics", type=CLASSIF)
public class IJstat_feature_ {

	Map<String, Integer > featureMap=new HashMap<>();
	
	
	String key="imagej_features";
	
	public  IJstat_feature_() {
		String headings[]=ResultsTable.getDefaultHeadings();
		for(int i=0; i<headings.length; i++) {

			featureMap.put(headings[i], i);
		}


	}

	public String getKey() {
		return this.key;
	}
	
	public Set<String> getFeatureNames(){
		return featureMap.keySet();
	}
	
	public double[] apply(ImageProcessor ip_roi){
		ResultsTable xx=new ResultsTable();
		Analyzer analyzer= new Analyzer(new ImagePlus("dummy",ip_roi),Measurements.ALL_STATS, xx);
		analyzer.run(ip_roi);
		String headings[]=xx.getHeadings();
		double outvector[]= new double[ResultsTable.getDefaultHeadings().length];
		for(int i=0; i<headings.length; i++) {
			double value=xx.getValue(headings[i], 0);
			outvector[featureMap.get(headings[i])]=value;
		}
		//keys.add(e)
		return outvector;
	}


}
