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
}
