package org.usf.jquery.mvc;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.usf.jquery.mvc.QueryModifier.Modifier.REPLACE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author u$f
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface QueryModifier {
	
	Modifier cte() default REPLACE;
	
	Modifier select() default REPLACE; // selected columns
	
	Modifier filter() default REPLACE; //merged with URL filters
	
	Modifier join() default  REPLACE;
	
	Modifier order() default  REPLACE;
	
	boolean acceptCriteria() default true;
	
	boolean overrideDistinct() default true;
	
	boolean overrideLimit() default true;
	
	boolean overrideOffset() default true;

	boolean overrideView() default true; //key-value
	
	public enum Modifier {
		
		REPLACE, MERGE, REJECT; 
	}
}
