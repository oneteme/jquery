package org.usf.jquery.web;

import static java.lang.String.format;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class NoSuchResourceException extends RuntimeException {

	public NoSuchResourceException(String s) {
		super(s);
	}

	static NoSuchResourceException noSuchResourceException(String resource) {
		return new NoSuchResourceException(format("no such resource '%s'", resource));
	}
	
	static NoSuchResourceException noSuchResourceException(String type, String name) {
		return noSuchResourceException(type, name, "");
	}
	
	static NoSuchResourceException noSuchResourceException(String type, String name, String parent) {
		return new NoSuchResourceException(format("no such resource %s[%s='%s']", parent, type, name));
	}
}
