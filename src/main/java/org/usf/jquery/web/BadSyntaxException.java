package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class BadSyntaxException extends WebException {

	public BadSyntaxException(String message) {
		super(message);
	}
	
	public static BadSyntaxException badSyntaxException(String o) {
		return new BadSyntaxException("bad synthax : " + o);
	}
}
