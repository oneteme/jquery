package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class WebException extends RuntimeException {

	public WebException(String message) {
		super(message);
	}

	public WebException(Throwable cause) {
		super(cause);
	}

	public WebException(String message, Throwable cause) {
		super(message, cause);
	}
}
