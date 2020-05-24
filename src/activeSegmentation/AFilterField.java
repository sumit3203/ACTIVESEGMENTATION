package activeSegmentation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

/**
 *  Annotation mechanism for tagging fields of classes
 * @author prodanov
 *
 */
public @interface AFilterField {
	/**
	 * unique key
	 * @return
	 */
	public String key() default "";
	
	/**
	 * The value is displayed in the GUI
	 * @return
	 */
	public String value() default "";
}
