package activeSegmentation.filter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import activeSegmentation.FilterType;

@Retention(RetentionPolicy.RUNTIME)
@interface AFilter {
   public String key();
   public String value();
   public FilterType type();
}