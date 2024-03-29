package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;

import java.util.NoSuchElementException;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
final class NoSuchResourceException extends NoSuchElementException {

	public NoSuchResourceException(String s) {
		super(s);
	}

	static NoSuchResourceException throwNoSuchTableException(String resource) {
		return noSuchResouceException("view", resource);
	}
	
	static NoSuchResourceException throwNoSuchColumnException(String resource) {
		return noSuchResouceException("column", resource);
	}
	
	static NoSuchResourceException noSuchResouceException(String type, String resource) {
		return new NoSuchResourceException(quote(resource)  + " " + type + " not found");
	}

	static NoSuchResourceException undeclaredResouceException(String view, String column) {
		return new NoSuchResourceException("column " + quote(column) + " is not declared in " + quote(view) + " view");
	}

}
