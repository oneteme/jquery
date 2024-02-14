package org.usf.jquery.web;

import static java.lang.String.join;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

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
		return new UnexpectedEntryException("unexpected entry : " + quote(entry) + 
				(isEmpty(expected) ? "" :" expect : " +  join("|", expected)));
	}
}
