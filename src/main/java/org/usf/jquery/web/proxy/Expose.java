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
public @interface Expose {
	
	/**
	 * whether to expose this method as resource, default is true, if false, this method will not be exposed as resource, even if it is annotated with @Bind.
	 */
	boolean value() default true;
	
	/**
	 * resource name, default is method name, if not empty, it will be used as resource name instead of method name.
	 */
	String identity() default "";
	
	/**
	 * alias for resource, default is empty, if not empty, it will be used as resource alias.
	 */
	String alias() default "";
	
	/**
	 * resource description, default is empty, it can be used to generate API documentation, if not empty, it will be used as resource description.
	 */
	String description() default ""; //html !?
}