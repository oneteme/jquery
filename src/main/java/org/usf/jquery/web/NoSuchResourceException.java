package org.usf.jquery.web;

import static java.lang.String.format;
import static org.usf.jquery.core.SqlStringBuilder.quote;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class NoSuchResourceException extends WebException {

	public NoSuchResourceException(String s) {
		super(s);
	}
	
	static NoSuchResourceException noSuchResourceException(String type, String resource) {
		return new NoSuchResourceException(format("no such %s: '%s'", type, resource));
	}

	static NoSuchResourceException undeclaredResouceException(String child, String parent) {
		return new NoSuchResourceException(quote(child) + " was not declared in " + quote(parent));
	}
}
