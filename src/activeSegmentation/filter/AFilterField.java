package activeSegmentation.filter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

public @interface AFilterField {
	public String key() default "";
	public String value() default "";
}
