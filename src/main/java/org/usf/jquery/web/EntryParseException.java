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


	static EntryParseException cannotParseEntryException(String type, RequestEntryChain entry) {
		return cannotParseEntryException(type, entry, null);
	}

	static EntryParseException cannotParseEntryException(String type, RequestEntryChain entry, Throwable cause) {
		return new EntryParseException(format("cannot parse %s : '%s'", entry, type), cause);
	}
}
