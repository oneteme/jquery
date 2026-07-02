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
public @interface QueryPolicy {

	/** Resources to exclude from the query. */
	String[] excludeResources() default {}; //support syntax: resource.*field, resource.* or resource.field
	
	/** Dialects to exclude from the query. */
	String[] excludeDialects() default {};
	
	/** If true, ensures the query is an aggregation. */
	boolean aggregate() default false; //??
	
	int maxRows() default 0;
		
	int maxCols() default 0;
}
