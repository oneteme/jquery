package org.usf.jquery.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestQueryParam2 {

	String view(); //view identity
	
	String database() default ""; 
	
	String[] column(); // selected columns
	
	String[] filters() default {}; //merged with URL filters
	
	String[] join() default {}; 
	
	String[] order() default {};
	
	boolean distinct() default false;
	
	int limit() default -1;
	
	int offset() default -1;
	
	String[] ignoreParameters() default {}; // will be not parsed
	
	String[] variables() default {}; // URL variable
}
