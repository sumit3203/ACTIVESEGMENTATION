package test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import activeSegmentation.filter.AFilterField;
import activeSegmentation.filter.LoG_Filter_;

public class testFilterAnn {

	@AFilterField(key="field", value="field annotation")
	public int afield=1;
	
	@AFilterField(key="field2", value="field annotation")
	public int afield2=2;
	
	@AFilterField(key="field3", value="field annotation")
	public int afield3=3;
	
	@AFilterField(key="field4", value="field annotation")
	public int afield4=3;
	
	public static void main (String[] args) {
		Class<testFilterAnn> c= testFilterAnn.class;
		 Annotation[] annotations;
	 
		 
		 // Find field annotations
	        try {
				annotations = AnnotationIntrospector.getFieldAnnotations(c, "afield");
				 System.out.println("Annotation on field  afield are:");
				 AnnotationIntrospector.printAnnotations(annotations);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	        System.out.println("All field annotations");
	    	
			Field [] fields = testFilterAnn.class.getFields();
			//Field [] fields = testFilterAnn.getClass().getFields();
			

			System.out.println(fields.length);
			
			for (Field field:fields)   {
				if (field.isAnnotationPresent(AFilterField.class)) {
					AFilterField fielda =  field.getAnnotation(AFilterField.class);
					System.out.println(field.toString());
			        System.out.println("key: " + fielda.key() +" value: " + fielda.value());
				}
			}
	        
		 
	}

}
