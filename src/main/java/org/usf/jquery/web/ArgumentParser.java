package org.usf.jquery.web;

import static java.util.Objects.isNull;
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
	
	Object parse(String v);

	default Object parseValue(String v) {
		try {
			return parse(v);
		}
		catch(Exception e) {
			throw cannotParseException("parameter value", v, e);
		}
	}

	default Object[] parseValues(String... args) {
		return args == null ? null : Stream.of(args).map(this::parseValue).toArray();
	}
	
	static Object tryParse(String value) {
		if(isNull(value)) {
			return null;
		}
		int i=0;
		Object o = null;
		while(i<PARSERS.size() && isNull(o = tryParse(value, PARSERS.get(i++))));
		return isNull(o) ? value : o;  //default type String
	}

	private static Object tryParse(String value, ArgumentParser parser) {
		try {
			return parser.parse(value);
		}
		catch(Exception e) {
			return null; //do not throw exception
		}
	}
}
