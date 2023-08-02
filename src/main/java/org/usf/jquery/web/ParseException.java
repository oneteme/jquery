package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class ParseException extends IllegalArgumentException {

	public ParseException(String message) {
		super(message);
	}
	
	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	static ParseException cannotEvaluateException(String type, String expression) {
		return new ParseException("cannot evaluate " + type +  " " + quote(expression));
	}
	
	static ParseException cannotParseException(String type, String value, Throwable cause) {
		return new ParseException("cannot parse " + type + " " + quote(value), cause);
	}

}
