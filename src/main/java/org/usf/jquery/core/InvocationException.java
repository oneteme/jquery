package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class InvocationException extends RuntimeException {

	public InvocationException(Throwable cause) {
		super(cause);
	}

	public InvocationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvocationException(String message) {
		super(message);
	}
}
