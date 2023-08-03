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
	
	String[] ignoreParameters() default {}; 
	
	@Deprecated(forRemoval = true)
	String[] defaultColumns() default {};
	
	@Deprecated
	boolean allowUnknownParameters() default false; //ignoreUnknownParameters
	
	boolean aggregationOnly() default false; 
	
	//TODO sortColumn, ..

	@Deprecated(forRemoval = true)
	String columnParameter() default "column"; 

	@Deprecated(forRemoval = true)
	String revisionParameter() default "revision";

	@Deprecated(forRemoval = true)
	RevisionMode revisionMode() default STRICT;
	
	@Deprecated(forRemoval = true)
	public enum RevisionMode {
		
		STRICT, CLOSEST;
	} 	
}