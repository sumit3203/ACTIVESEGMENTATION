package test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class testFilter {

	@FilterField("fancy field")
	public int afield=1;
	
	public testFilter() {
		// TODO Auto-generated constructor stub
	}
	
	 
	public static void main (String[] args) {
		Class<testFilter> c= testFilter.class;
		 Annotation[] annotations;
		 AnnotationIntrospector ai = new AnnotationIntrospector();
		 
		 // Find field annotations
	        annotations = ai.findFieldAnnotation(c, "afield");
	        System.out.println("Annotation on field  afield are:");
	        AnnotationIntrospector.showAnnotations(annotations);
	        
	    		
			Field [] fields = testFilter.class.getFields();
			for (Field field:fields)   {
				Annotation annotation = field.getAnnotation(FilterField.class);
				// System.out.println("///");
				 System.out.println(annotation.toString());
		        if(annotation instanceof FilterField){
		        	FilterField customAnnotation = (FilterField) annotation;
		           System.out.println("name: " + customAnnotation.value());
		        }
			}
		 
	        
		 
	}

}
