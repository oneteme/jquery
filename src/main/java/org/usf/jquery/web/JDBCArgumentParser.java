package org.usf.jquery.web;

import static org.usf.jquery.web.EntryChain.cannotParseEntryException;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JDBCArgumentParser extends JavaArgumentParser {
	
	Object nativeParse(String v);
	
	@Override
	default Object parseEntry(EntryChain entry, QueryContext td) {
		try {
			return nativeParse(entry.toString()); //!value
		}
		catch(Exception e) {
			throw cannotParseEntryException(entry, "", e);
		}
	}
}
