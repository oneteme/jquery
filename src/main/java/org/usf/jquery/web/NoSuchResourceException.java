package org.usf.jquery.web;

import static java.lang.String.format;
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
		return noSuchResourceException("database", resource);
	}

	static NoSuchResourceException noSuchViewException(String resource) {
		return noSuchResourceException(VIEW, resource);
	}
	
	static NoSuchResourceException noSuchColumnException(String resource) {
		return noSuchResourceException(COLUMN, resource);
	}

	static NoSuchResourceException noSuchResourceException(String type, String resource) {
		return new NoSuchResourceException(format("%s='%s'", type, resource));
	}

	static NoSuchResourceException undeclaredResouceException(String child, String parent) {
		return new NoSuchResourceException(quote(child) + " was not declared in " + quote(parent));
	}
}
