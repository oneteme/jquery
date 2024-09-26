package org.usf.jquery.web;

import static java.lang.String.format;

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

	public static ResourceAccessException resourceAlreadyExistsException(String name) {
		return new ResourceAccessException(format("an other ressource with name='%s' is already exists", name));
	}
}
