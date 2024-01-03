package org.usf.jquery.web;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface JDBCArgumentParser extends JavaArgumentParser {
	
	Object nativeParse(String v);
	
	@Override
	default Object parse(RequestEntryChain entry, TableDecorator td) {
		return parse(entry.requireNoArgs().getValue());
	}

	default Object[] parseAll(String... args) {
		return isNull(args) ? null : Stream.of(args).map(this::parse).toArray();
	}

	default Object parse(String v) {
		try {
			return nativeParse(v);
		}
		catch(Exception e) {
			throw new ParseException(format("cannot parse %s value '%s'", toString(), v), e); 
		}
	}
}
