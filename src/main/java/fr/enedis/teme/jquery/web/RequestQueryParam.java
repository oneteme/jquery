package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.web.RequestQueryParam.RevisionMode.STRICT;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.enedis.teme.jquery.DBTable;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestQueryParam {

	Class<? extends Enum<? extends DBTable>> value();

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