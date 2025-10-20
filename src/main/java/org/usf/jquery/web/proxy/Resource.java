package org.usf.jquery.web.proxy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Resource {

	String value() default "";

	String description() default ""; //html !?
	
	String tagname() default ""; //???
	
	Class<ArgsResolver> resolver() default ArgsResolver.class; 
}