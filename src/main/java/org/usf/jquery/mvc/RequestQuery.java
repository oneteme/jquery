package org.usf.jquery.mvc;

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
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestQuery { //RequestQuery

	/** The target table or view name. */
	String dataset();
	
	/** The database or schema identity. */
	Class<? extends StoreResource> store() default StoreResource.class;

	/** Default columns to select if none are provided in the request. */
	String[] fields() default {};

	/** Parameters to be ignored by the interpreter. */
	String[] ignore() default {};
	
	String view() default "map";
}