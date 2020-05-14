package activeSegmentation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ijaux.datatype.Pair;


public interface IAnnotated {
	
	/**
	 * returns annotations of the public(!) fields
	 * @return Map<String, String>
	 */
	default Map<String, String> getAnotatedFileds(){		
		final Map< String, String > afields= new HashMap<String, String>();
		final Field [] fields = this.getClass().getFields();
		for (Field field:fields)   {
			if (field.isAnnotationPresent(AFilterField.class)) {
				AFilterField fielda =  field.getAnnotation(AFilterField.class);
		        afields.put(fielda.key(), fielda.value());
			}
		}
		
		return afields;
	 
	}
	
	/**
	 * returns the filter type declared in the class-level annotation
	 * @return FilterType
	 */
	default public FilterType getAType() {
		Class<?> c= this.getClass();
		final Annotation[] arran=AnnotationManager.getClassAnnotations(c);
		for (Annotation aa:arran  ) {       //Iterator on array arran
			if (aa instanceof AFilter) {
				final AFilter af= ((AFilter)aa);		 
				FilterType ft=af.type();
				return ft;
			}
			  
		}
		//throw new RuntimeException("No filter annotations present");
		System.out.println("No filter annotations present");
		return FilterType.NONE;
	}
	
	
	default public Pair<String,String> getKeyVal() {
		Class<?> c= this.getClass();
		final Annotation[] arran=AnnotationManager.getClassAnnotations(c);
		for (Annotation aa:arran  ) {     //Iterator on arran array
			
			if (aa instanceof AFilter) {
				final AFilter af= ((AFilter)aa);		 
				return Pair.of(af.key(),af.value());
			}
			  
		}
		//throw new RuntimeException("No filter annotations present");
		System.out.println("No filter annotations present");
		return Pair.of("NONE","NONE");
	}

}
