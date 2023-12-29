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

	@Deprecated
	static ParseException cannotEvaluateException(String type, String expression) {
		return new ParseException("cannot evaluate " + type +  " " + quote(expression));
	}

	@Deprecated
	static ParseException cannotParseException(String type, String value, Throwable cause) {
		return new ParseException("cannot parse " + type + " " + quote(value), cause);
	}

}
