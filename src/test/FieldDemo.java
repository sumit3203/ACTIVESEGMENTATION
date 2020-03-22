package test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class FieldDemo {

   public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
      Field field = SampleClass.class.getField("sampleField");
      Annotation annotation = field.getAnnotation(CustomAnnotation.class);
      if(annotation instanceof CustomAnnotation){
         CustomAnnotation customAnnotation = (CustomAnnotation) annotation;
         System.out.println("name: " + customAnnotation.name());
         System.out.println("value: " + customAnnotation.value());
      }
   }
}

@CustomAnnotation(name = "SampleClass",  value = "Sample Class Annotation")
class SampleClass {

   @CustomAnnotation(name="sampleClassField",  value = "Sample Field Annotation")
   public String sampleField;
    
   public String getSampleField() {
      return sampleField;
   }

   public void setSampleField(String sampleField) {
      this.sampleField = sampleField;
   }
}

@Retention(RetentionPolicy.RUNTIME)
@interface CustomAnnotation {
   public String name();
   public String value();
}
