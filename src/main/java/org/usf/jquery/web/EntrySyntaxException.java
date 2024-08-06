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
	
	static EntrySyntaxException requireEntryTagException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("%s:<tag> required", entry));
	}
	
	static EntrySyntaxException requireNoArgsEntryException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("%s takes no args : %s", entry.getValue(), entry));
	}

	static EntrySyntaxException unexpectedEntryException(RequestEntryChain entry) {
		return new EntrySyntaxException(format("unexpected entry : %s", entry));
	}
}
