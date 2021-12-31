package fr.enedis.teme.jquery.web;

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

	String[] names() default {};
	
	Mode mode() default Mode.INCLUDE;
	
	boolean columns() default true;
	
	boolean filters() default true;
	
	String columnParameter() default "column";

	String revisionParameter() default "revision";
	
	enum Mode {
		
		INCLUDE, EXCLUDE;
	}
	
}