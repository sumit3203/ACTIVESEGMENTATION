package test;

import static activeSegmentation.FilterType.SEGM;

import java.awt.Image;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.AFilter;
import activeSegmentation.AFilterField;
import activeSegmentation.AnnotationManager;
import activeSegmentation.FilterType;
import activeSegmentation.IFilter;
import ij.Prefs;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.datatype.Pair;

@AFilter(key="TF", value="Test Filter", type=SEGM, help = "help.html")
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
	
	/**
	 * returns the filter type declared in the class-level annotation
	 * @return FilterType
	 */
	 @Override
	public String getHelpResource() {
		Class<?> c= this.getClass();
		final Annotation[] arran=AnnotationManager.getClassAnnotations(c);
		for (Annotation aa:arran  ) {
			//System.out.println("AA: " +aa);
			if (aa instanceof AFilter) {
				//System.out.println("AF: " +aa);
				final AFilter af= ((AFilter)aa);		 
				return af.help();
			}
			  
		}
		//throw new RuntimeException("No filter annotations present");
		System.out.println("No filter annotations present");
		return "";
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

	

	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		TestFilter filter = new TestFilter();
		//filter.getDefaultSettings();
		Map<String, String> fanot=filter.getAnotatedFileds();
		System.out.println(fanot);
		 
		//if (filter.getClass().isAnnotationPresent(AFilter.class)) 
		/*
		 * Annotation[] arran=AnnotationManager.getClassAnnotations(filter.getClass());
		 * AnnotationManager.printAnnotations(arran); for (Annotation aa:arran ) { if
		 * (aa instanceof AFilter) { AFilter af= ((AFilter)aa); String skey=af.key();
		 * FilterType ft=af.type(); Pair<String, FilterType> p=Pair.of(skey, ft);
		 * System.out.println(p); }
		 * 
		 * }
		 */
		
		
		FilterType ft=filter.getAType();
		System.out.println(ft);
		
		String hlp=filter.getHelpResource();
		
		System.out.println(hlp);

		
		 new Thread() {
	            @Override
	            public void run() {
	                javafx.application.Application.launch(WebHelper.class, hlp);
	            }
	        }.start();
		
		
	}

	/**
	 * @param arran
	 */
	/*
	 * public FilterType getAType(Class<?> c) { final Annotation[]
	 * arran=AnnotationManager.getClassAnnotations(c); for (Annotation aa:arran ) {
	 * System.out.println(aa); if (aa instanceof AFilter) { final AFilter af=
	 * ((AFilter)aa); FilterType ft=af.type(); return ft; }
	 * 
	 * } return null; }
	 */

	@Override
	public FilterType getFilterType() {
		return FilterType.SEGM;
	}

}
