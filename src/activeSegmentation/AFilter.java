package activeSegmentation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AFilter {
   public String key();
   public String value();
   public FilterType type();
}