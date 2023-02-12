package activeSegmentation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation mechanism for tagging classes
 * @author prodanov
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AFilter {
	
	/**
	 * unique key used in the GUI 
	 * @return String
	 */
   public String key();
   
   /**
    * The value is displayed in the GUI as a tab title
    * @return String
    */
   public String value();
   
   /**
    * The type is used in the loading mechanism
    * 
    * @return FilterType
    */
   public FilterType type();
   
   /**
    * Help resource
    * @return String
    */
   public String help();
   
}