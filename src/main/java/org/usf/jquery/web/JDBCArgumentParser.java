package org.usf.jquery.web;

import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JDBCArgumentParser extends JavaArgumentParser {
	
	Object nativeParse(String v);
	
	@Override
	default Object parseEntry(RequestEntryChain entry, ViewDecorator td) {
		try {
			return nativeParse(entry.requireNoArgs().requireNoNext().getValue());
		}
		catch(Exception e) {
			throw cannotParseEntryException("value", entry, e);
		}
	}
}
