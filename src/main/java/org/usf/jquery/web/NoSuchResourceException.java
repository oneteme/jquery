package org.usf.jquery.web;

import static java.lang.String.format;

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
		return new NoSuchResourceException(format("'%s' is not member of '%s'", child, parent));
	}
	
	static NoSuchResourceException noSuchResourceException(String name, String parent, String resource) {
		return new NoSuchResourceException(format("no such resource %s[%s='%s']", name, parent, resource));
	}
}
