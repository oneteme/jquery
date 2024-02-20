package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class ParseException extends WebException {

	public ParseException(String message) {
		super(message);
	}
	
	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	static ParseException cannotParseException(String type, String value) {
		return cannotParseException(type, value, null);
	}

	static ParseException cannotParseException(String type, String value, Throwable cause) {
		return new ParseException(formatMessage("cannot parse entry", type, value), cause);
	}
}
