package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class BadArgumentException extends JQueryException {

	public BadArgumentException(String message) {
		super(message);
	}
	
	public BadArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}