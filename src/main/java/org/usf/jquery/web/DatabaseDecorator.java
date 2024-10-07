package org.usf.jquery.web;

import static org.usf.jquery.web.ContextManager.context;

import org.usf.jquery.core.QueryBuilder;

/**
 * 
 * @author u$f
 * 
 */
public interface DatabaseDecorator {

	String identity(); //URL
	
	String viewName(ViewDecorator vd); //[schema.]table
	
	default QueryBuilder newQuery() {
		var ctx = context(identity());
		return new QueryBuilder(ctx.getMetadata().getType()); 
	}
}
