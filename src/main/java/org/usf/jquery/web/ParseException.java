package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class ParseException extends IllegalArgumentException {

	private ParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	static ParseException parseException(String param, Throwable cause) {
		return new ParseException("cannot parse value '" + param + "'", cause);
	}

}
