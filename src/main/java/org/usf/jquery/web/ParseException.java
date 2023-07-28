package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class ParseException extends IllegalArgumentException {

	public ParseException(String message) {
		super(message);
	}
	
	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	static ParseException cannotEvaluateException(String type, String expression) {
		return new ParseException("cannot evaluate " + type +  " '" + expression + "' ");
	}
	
	static ParseException cannotParseException(String type, String value, Throwable cause) {
		return new ParseException("cannot parse " + type + " '" + value + "' ", cause);
	}

}
