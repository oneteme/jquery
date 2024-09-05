package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class ResourceAccessException extends WebException { //read & write access

	public ResourceAccessException(String message) {
		super(message);
	}

	public static ResourceAccessException resourceAlreadyExistsException(String name, Object value) {
		return new ResourceAccessException(quote(name) + " is already exists : " + value);
	}
}
