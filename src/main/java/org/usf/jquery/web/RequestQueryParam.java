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
	
	@Deprecated
	boolean allowUnknownParameters() default false; //ignoreUnknownParameters
	
	String[] ignoreParameters() default {};
	
	//TODO sortColumn, ..
	
	@Deprecated
	String columnParameter() default "column"; 

	@Deprecated
	String revisionParameter() default "revision";

	@Deprecated
	RevisionMode revisionMode() default STRICT;
	
	@Deprecated
	public enum RevisionMode {
		
		STRICT, CLOSEST;
	} 	
}