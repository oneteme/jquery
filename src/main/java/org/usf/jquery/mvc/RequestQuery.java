package org.usf.jquery.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author u$f
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestQuery {

	/** The target table or view name. */
	String dataset();
	
	/** The database or schema identity. */
	Class<? extends StoreResource> store() default StoreResource.class; 

	String[] cte() default {}; // selected columns
	
	/** Default columns to select if none are provided in the request. */
	String[] select(); // selected columns
	
	String[] join() default {}; 
	
	String[] order() default {};
	
	String[] criteria() default {}; //merged with URL filters
	
	boolean distinct() default false;
	
	int limit() default 0;
	
	int offset() default 0;

	/** Parameters to be ignored by the interpreter. */
	String[] ignore() default {};
	
	String view() default "map"; //key-value
}