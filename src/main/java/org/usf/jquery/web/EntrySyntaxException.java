package org.usf.jquery.web;

/**
 * Signals that a entry syntax is invalid.
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class EntrySyntaxException extends WebException {

	public EntrySyntaxException(String message) {
		super(message);
	}
	
	public EntrySyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
}
