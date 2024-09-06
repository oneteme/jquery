package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class JQueryException extends RuntimeException {

	public JQueryException(String message) {
		super(message);
	}

	public JQueryException(Throwable cause) {
		super(cause);
	}

	public JQueryException(String message, Throwable cause) {
		super(message, cause);
	}
}
