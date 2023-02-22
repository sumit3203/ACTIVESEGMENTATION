package activeSegmentation.moment;

import static activeSegmentation.FilterType.CLASSIF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.AFilter;
import activeSegmentation.IMoment;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;


@AFilter(key="imagej_features", value="IJ ROI statistics", type=CLASSIF, help = "")
public class IJstat_feature_ implements IMoment<ArrayList<?>>{

	Map<String, Integer > featureMap=new HashMap<>();
	
	private boolean isEnabled=true;
	
//	String key="imagej_features";
	
	public  IJstat_feature_() {
		String headings[]=ResultsTable.getDefaultHeadings();
		for(int i=0; i<headings.length; i++) {

			featureMap.put(headings[i], i);
		}


	}

	/*
	public String getKey() {
		return this.key;
	}
	*/
	
	@Override
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

	@Override
	public Map<String, String> getDefaultSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public boolean reset() {
		return false;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.isEnabled=isEnabled;
	}

	@Override
	public ArrayList<?> getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<String, double[]> apply(ImageProcessor imageProcessor, Roi roi) {
		// TODO Auto-generated method stub
		return null;
	}


}
