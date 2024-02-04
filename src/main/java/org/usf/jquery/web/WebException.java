package org.usf.jquery.web;

import org.usf.jquery.core.JqueryException;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class WebException extends JqueryException {

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
