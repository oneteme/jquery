package org.usf.jquery.web;

import static java.lang.String.format;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class EntryParseException extends WebException {

	public EntryParseException(String message) {
		super(message);
	}
	
	public EntryParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	static EntryParseException cannotParseEntryException(String type, String value) {
		return cannotParseEntryException(type, value, null);
	}

	static EntryParseException cannotParseEntryException(String type, String value, Throwable cause) {
		return new EntryParseException(format("cannot parse %s : '%s'", type, value), cause);
	}
	
	static EntryParseException unexpectedEntryException(String entry) {
		return new EntryParseException(format("unexpected entry : '%s'", entry));
	}
	
	static EntryParseException requireEntryException(String name) {
		return new EntryParseException(name + " required");
	}
}
