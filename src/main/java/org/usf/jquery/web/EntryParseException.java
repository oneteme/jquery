package org.usf.jquery.web;

/**
 * Signals that cannot be parse entry.
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class EntryParseException extends RuntimeException {

	public EntryParseException(String message) {
		super(message);
	}
	
	public EntryParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
