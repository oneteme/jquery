package org.usf.jquery.web;

import static org.usf.jquery.web.RequestQueryParam.RevisionMode.STRICT;

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

	String name(); //tablename
	
	String[] defaultColumns() default {};
	
	boolean allowUnknownParameters() default false; //ignoreUnknownParameters
	
	//TODO sortColumn, ..
	
	String columnParameter() default "column"; 
	
	String revisionParameter() default "revision";
	
	RevisionMode revisionMode() default STRICT;
	
	
	public enum RevisionMode {
		
		STRICT, CLOSEST;
	} 	
}