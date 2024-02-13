package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;

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

	static ParseException cannotParseException(String type, String value, Throwable cause) {
		return new ParseException("cannot parse " + type + " " + quote(value), cause);
	}
}
