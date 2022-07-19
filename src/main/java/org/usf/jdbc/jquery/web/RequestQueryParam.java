package org.usf.jdbc.jquery.web;

import static org.usf.jdbc.jquery.web.RequestQueryParam.RevisionMode.STRICT;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestQueryParam {

	String name(); //tablename
	
	boolean columns() default true;
	
	String[] defaultColumns() default {};
	
	boolean filters() default true;
	
	boolean allowUnknownParameters() default false;
	
	String columnParameter() default "column";

	String revisionParameter() default "revision";
	
	RevisionMode revisionMode() default STRICT;
	
	
	public enum RevisionMode {
		
		STRICT, CLOSEST;
	} 	
}