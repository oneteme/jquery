package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.VIEW;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
final class NoSuchResourceException extends WebException {

	public NoSuchResourceException(String s) {
		super(s);
	}

	static NoSuchResourceException noSuchDatabaseException(String resource) {
		return noSuchResouceException(VIEW, resource);
	}

	static NoSuchResourceException noSuchViewException(String resource) {
		return noSuchResouceException(VIEW, resource);
	}
	
	static NoSuchResourceException noSuchColumnException(String resource) {
		return noSuchResouceException(COLUMN, resource);
	}
	
	static NoSuchResourceException noSuchResouceException(String type, String resource) {
		return new NoSuchResourceException(quote(resource)  + " " + type + " not found");
	}

	static NoSuchResourceException undeclaredResouceException(String child, String parent) {
		return new NoSuchResourceException(quote(child) + " was not declared in " + quote(parent));
	}
}
