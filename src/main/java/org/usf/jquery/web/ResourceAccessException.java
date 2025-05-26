package org.usf.jquery.web;

/**
 * 
 * 	This exception is thrown when a resource is not accessible.
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class ResourceAccessException extends WebException {

	public ResourceAccessException(String message) {
		super(message);
	}
}
