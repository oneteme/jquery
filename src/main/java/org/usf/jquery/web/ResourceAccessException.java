package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class ResourceAccessException extends WebException { //read & write access

	public ResourceAccessException(String message) {
		super(message);
	}

	public static ResourceAccessException resourceAlreadyExistsException(String name, String value) {
		return new ResourceAccessException(name + " already exists : " + value);
	}


	public static ResourceAccessException accessDeniedException(String reason) {
		return new ResourceAccessException("access denied : " + reason);
	}
}
