package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class ConflictingResourceException extends WebException {

	public ConflictingResourceException(String message) {
		super(message);
	}

	public static ConflictingResourceException resourceAlreadyExistsException(String name, String value) {
		return new ConflictingResourceException(name + " already exists : " + value);
	}
}
