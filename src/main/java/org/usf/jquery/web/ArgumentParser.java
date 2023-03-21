package org.usf.jquery.web;

import static org.usf.jquery.web.ParameterInvalidValueException.invalidParameterValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface ArgumentParser {

	Object parseArg(String arg); //TD throw invalidParameterValueException(value, e);

	default Object[] parseArgs(String... args) {
		List<Object> list = new ArrayList<>(args.length);
		Function<String, Object> parser = this::parseArg;
		for(String value : args) {
			try {
				list.add(parser.apply(value));
			}
			catch(Exception e) {
				throw invalidParameterValueException(value, e);
			}
		}
		return list.toArray();
	}
}
