package activeSegmentation.filter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface AFilter {
   public String key();
   public String value();
}