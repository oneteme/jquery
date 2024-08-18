package org.usf.jquery.web;

import static java.lang.String.format;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class EntrySyntaxException extends WebException {

	public EntrySyntaxException(String message) {
		super(message);
	}
	
	public EntrySyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	static EntrySyntaxException unexpectedEntryException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("unexpected entry : %s", entry));
	}

	static EntrySyntaxException unexpectedEntryValueException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("unexpected value : %s", entry));
	}
	
	static EntrySyntaxException unexpectedEntryArgsException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("%s takes no args : %s", entry.getValue(), entry));
	}

	static EntrySyntaxException badEntryArgsException(RequestEntryChain entry, Exception e) {
		return new EntrySyntaxException("bad argument : " + entry, e);
	}
	
	static EntrySyntaxException expectedEntryTagException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("expected <tag> after '%s'", entry));
	}
}
