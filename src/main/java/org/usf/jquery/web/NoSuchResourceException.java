package org.usf.jquery.web;

import java.util.NoSuchElementException;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class NoSuchResourceException extends NoSuchElementException {

	public NoSuchResourceException(String s) {
		super(s);
	}

	//pretty exception throw
	static <T> T throwNoSuchTableException(String resource) {
		throw noSuchResouceException("table", resource);
	}
	
	static <T> T throwNoSuchColumnException(String resource) {
		throw noSuchResouceException("column", resource);
	}
	
	static NoSuchResourceException noSuchResouceException(String type, String resource) {
		return new NoSuchResourceException(type + " '" + resource + "' " + "not found");
	}

}
