package org.usf.jquery.web;

import static java.lang.String.join;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public class UnexpectedEntryException extends WebException {

	public UnexpectedEntryException(String message) {
		super(message);
	}
	
	public static UnexpectedEntryException unexpectedEntryException(String entry, String... expected) {
		return new UnexpectedEntryException(formatMessage("unexpected entry, ", join("|", expected), entry));
	}
}
