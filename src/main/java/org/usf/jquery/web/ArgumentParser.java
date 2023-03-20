package org.usf.jquery.web;

import static org.usf.jquery.web.ParameterInvalidValueException.invalidParameterValueException;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface ArgumentParser {

	Object parseArg(String arg);

	default Object[] parseArgs(String... args) {
		List<Object> list = new ArrayList<>(args.length);
		for(String value : args) {
			try {
				list.add(parseArg(value));
			}
			catch(Exception e) {
				throw invalidParameterValueException(value, e);
			}
		}
		return list.toArray();
	}
}
