package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class ResourceMappingException extends RuntimeException {

	public ResourceMappingException(String message) {
		super(message);
	}

	public ResourceMappingException(Throwable cause) {
		super(cause);
	}

	public ResourceMappingException(String message, Throwable cause) {
		super(message, cause);
	}
}
