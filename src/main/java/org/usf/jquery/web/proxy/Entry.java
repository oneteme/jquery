package org.usf.jquery.web.proxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author u$f
 *
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Entry { //or expose

	String value() default "";

	String description() default ""; //html !?
	
	String tagname() default ""; //for DBColumn only
}