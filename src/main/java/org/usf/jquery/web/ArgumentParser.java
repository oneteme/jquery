package org.usf.jquery.web;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ArgumentParser {
	
	Object parse(String arg);

	default Object parseArg(String arg) {
		try {
			return parse(arg);
		}
		catch(Exception e) {
			throw new IllegalArgumentException("cannot parse value " + arg, e);
		}
	}

	default Object[] parseArgs(String... args) {
		return args == null ? null : Stream.of(args).map(this::parseArg).toArray();
	}
}
