package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class JQueryConfigurationException extends RuntimeException {

	public JQueryConfigurationException(String message) {
		super(message);
	}

	public JQueryConfigurationException(Throwable cause) {
		super(cause);
	}

	public JQueryConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
