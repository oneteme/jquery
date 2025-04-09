package org.usf.jquery.web;

import static org.usf.jquery.web.ContextManager.context;

import org.usf.jquery.core.QueryComposer;

/**
 * 
 * @author u$f
 * 
 */
public interface DatabaseDecorator {

	String identity(); //URL
	
	String viewName(ViewDecorator vd); //[schema.]table
	
	default QueryComposer newQuery() {
		return context(identity()).getCurrentQuery();
	}
}
