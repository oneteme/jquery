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

	static EntrySyntaxException badEntryArgsException(RequestEntryChain entry, Exception e) {
		return new EntrySyntaxException("bad argument : " + entry, e);
	}	
	

	static EntrySyntaxException badEntrySyntaxException(String type, String value) {
		return badEntrySyntaxException(type, value, null);
	}

	static EntrySyntaxException badEntrySyntaxException(String type, String value, Exception e) {
		return new EntrySyntaxException(format("bad %s : %s", type, value), e);
	}
	
}
