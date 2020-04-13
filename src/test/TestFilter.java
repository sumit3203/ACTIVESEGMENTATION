package test;

import java.awt.Image;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.FilterType;
import activeSegmentation.IFilter;
import activeSegmentation.filter.AFilterField;
import ij.Prefs;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class TestFilter implements IFilter {

	/*
	 * apparently only public fields can be annotated
	 */
	public final static String SIGMA="A", LEN="B", MAX_LEN="C", ISSEP="D", SCNORM="E";

	@AFilterField(key=ISSEP, value="separable")
	public boolean sep= Prefs.getBoolean(ISSEP, true);

	@AFilterField(key=SCNORM, value="normalized")
	public boolean scnorm= Prefs.getBoolean(SCNORM, false);
	
	@AFilterField(key=LEN, value="size")
	public int sz= Prefs.getInt(LEN, 2);
	
	/*
	 * max_sz not accessible
	 */
	@AFilterField(key=MAX_LEN, value="max size")
	int max_sz= Prefs.getInt(MAX_LEN, 8);
	
	public TestFilter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		Field [] fields = TestFilter.class.getFields();
		System.out.println("fields "+fields.length);
		
		for (Field field:fields)   {
			if (field.isAnnotationPresent(AFilterField.class)) {
				AFilterField fielda =  field.getAnnotation(AFilterField.class);
				//System.out.println(field.toString());
		        System.out.println("key: " + fielda.key() +" value: " + fielda.value());
			}
		}
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
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reset() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub

	}

	

	public static void main(String[] args) {
		TestFilter filter = new TestFilter();
		//filter.getDefaultSettings();
		filter.getAnotatedFileds();

	}

	@Override
	public FilterType getFilterType() {
		return FilterType.SEGM;
	}

}
