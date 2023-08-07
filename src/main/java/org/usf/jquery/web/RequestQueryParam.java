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
	
	String[] ignoreParameters() default {}; 
	
	boolean aggregationOnly() default false; 
	
	/**
	 * 
	 * @see RequestQueryParam::ignoreParameters
	 */
	@Deprecated(forRemoval = true)
	boolean allowUnknownParameters() default false; //ignoreUnknownParameters
	
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