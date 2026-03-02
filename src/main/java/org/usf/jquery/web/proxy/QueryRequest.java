package org.usf.jquery.web.proxy;

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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryRequest {

	String view(); //view identity
	
	Class<? extends SchemaResource> database() default SchemaResource.class; //optional database identity
	
	String[] defaultColumns() default {};
	
	String[] ignoreParameters() default {}; // will be not parsed
	
	boolean aggregationOnly() default false; // else throw IllegalDataAccessException
	
	int maxRows() default -1; //max rows to return, -1 for no limit, else throw IllegalDataAccessException
}