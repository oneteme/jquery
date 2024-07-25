package org.usf.jquery.web;

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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestQueryParam {

	String view(); //view identity
	
	String database() default ""; //optional database identity
	
	String[] defaultColumns() default {};
	
	String[] ignoreParameters() default {}; // !parsed 
	
	boolean aggregationOnly() default false; // else throw IllegalDataAccessException
}