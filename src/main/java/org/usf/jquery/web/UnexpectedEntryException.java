package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class UnexpectedEntryException extends WebException {

	public UnexpectedEntryException(String message) {
		super(message);
	}
	
	public static UnexpectedEntryException unexpectedEntryException(String o) {
		return new UnexpectedEntryException("unexpected entry : " + o);
	}
}
