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
public @interface QueryGuard {

	/** Resources to exclude from the query. */
	String[] excludeResources() default {}; //TD support syntax: resource.*field, resource.* or resource.field
	
	/** Dialects to exclude from the query. */
	String[] excludeDialects() default {};
	
	/** If true, ensures the query is an aggregation. */
	boolean aggregate() default false; //??
	
	/** If true, ensures that returned result is lower than the specified limit. */
	int maxRows() default 0;
		
	/** If true, ensures that returned result has lower than the specified number of columns. */
	int maxCols() default 0;
}
