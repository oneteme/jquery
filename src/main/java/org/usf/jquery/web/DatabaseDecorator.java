package org.usf.jquery.web;

import static org.usf.jquery.web.ContextManager.context;

import org.usf.jquery.core.RequestComposer;

/**
 * 
 * @author u$f
 * 
 */
public interface DatabaseDecorator {

	String identity(); //URL
	
	String viewName(ViewDecorator vd); //[schema.]table
	
	default RequestComposer newQuery() {
		return context(identity()).getCurrentQuery();
	}
}
