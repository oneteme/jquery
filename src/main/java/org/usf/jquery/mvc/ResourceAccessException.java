package org.usf.jquery.mvc;

/**
 * 
 * 	This exception is thrown when a resource is not accessible.
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class ResourceAccessException extends RuntimeException {

	public ResourceAccessException(String message) {
		super(message);
	}
}
