package org.usf.jquery.web;

import static org.usf.jquery.web.ParseException.cannotParseException;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JDBCArgumentParser extends JavaArgumentParser {
	
	Object nativeParse(String v);
	
	@Override
	default Object parseEntry(RequestEntryChain entry, TableDecorator td) {
		return parseValue(entry.requireNoArgs().getValue());
	}

	default Object parseValue(String v) {
		try {
			return nativeParse(v);
		}
		catch(Exception e) {
			throw cannotParseException(toString(), v, e); 
		}
	}
}
