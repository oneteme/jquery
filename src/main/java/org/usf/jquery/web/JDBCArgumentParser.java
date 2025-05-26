package org.usf.jquery.web;

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
			throw EntryChain.cannotParseEntryException(entry, "", e);
		}
	}
}
