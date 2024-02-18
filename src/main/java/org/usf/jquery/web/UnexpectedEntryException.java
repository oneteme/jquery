package org.usf.jquery.web;

import static java.lang.String.join;
import static org.usf.jquery.core.SqlStringBuilder.quote;

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
		return new UnexpectedEntryException("unexpected entry, " +
				"expected: " +  join("|", expected) +
				" but was: " + quote(entry));
	}
}
