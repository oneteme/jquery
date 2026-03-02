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

	/** The target table or view name. */
	String dataset();
	
	/** The database or schema identity. */
	Class<? extends Store> store() default Store.class; //optional database identity

	/** Default columns to select if none are provided in the request. */
	String[] fields() default {};

	/** Parameters to be ignored by the interpreter. */
	String[] ignore() default {};
	
	/** If true, ensures the query is an aggregation. */
	boolean aggregate() default false;

	/** Hard limit for the number of rows returned. */
	int maxSize() default -1; //max rows to return, -1 for no limit, else throw IllegalDataAccessException
}