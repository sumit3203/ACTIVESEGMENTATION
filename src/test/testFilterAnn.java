package test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class testFilterAnn {

	@FilterField("fancy field")
	public int afield=1;
	 
	 
	public static void main (String[] args) {
		Class<testFilterAnn> c= testFilterAnn.class;
		 Annotation[] annotations;
		 //AnnotationIntrospector ai = new AnnotationIntrospector();
		 
		 // Find field annotations
	        try {
				annotations = AnnotationIntrospector.getFieldAnnotations(c, "afield");
				 System.out.println("Annotation on field  afield are:");
				 AnnotationIntrospector.printAnnotations(annotations);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	        
	    		
			Field [] fields = testFilterAnn.class.getFields();
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
