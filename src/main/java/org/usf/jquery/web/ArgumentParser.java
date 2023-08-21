package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.Constants.PARSERS;
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

	default Object parse(String v) {
		try {
			return nativeParse(v);
		}
		catch(Exception e) {
			throw cannotParseException("parameter value", v, e);
		}
	}

	default Object[] parseAll(String... args) {
		return isNull(args) ? null : Stream.of(args).map(this::parse).toArray();
	}
	
	static Object tryParse(String value) {
		if(nonNull(value)) {
			for(var p : PARSERS) {
				try {
					return p.nativeParse(value);
				}
				catch (Exception e) {/* do not handle exception */}
			}
		}
		return value;  //default type String
	}
}
