package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.web.ParseException.cannotParseException;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ArgumentParser {
	
	Object nativeParse(String v);

	default Object[] parseAll(String... args) {
		return isNull(args) ? null : Stream.of(args).map(this::parse).toArray();
	}

	default Object parse(String v) {
		try {
			return nativeParse(v);
		}
		catch(Exception e) {
			throw cannotParseException("parameter value", v, e);
		}
	}
	
	default Object tryParse(String v) {
		try {
			return nativeParse(v);
		}
		catch(Exception e) {
			return null;
		}
	}
	
}
