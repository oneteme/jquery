package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class JqueryException extends RuntimeException {

	public JqueryException(String message) {
		super(message);
	}

	public JqueryException(Throwable cause) {
		super(cause);
	}

	public JqueryException(String message, Throwable cause) {
		super(message, cause);
	}
}
