package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
@SuppressWarnings("serial")
public final class BadArgumentException extends JQueryException {

	public BadArgumentException(String message) {
		super(message);
	}
	
	public BadArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public static BadArgumentException badArgumentTypeException(JavaType[] types, Object actual) {
		return badArgumentTypeException(types, actual, null);
	}
	
	public static BadArgumentException badArgumentTypeException(JavaType[] types, Object actual, Exception e) {
		return new BadArgumentException(formatMessage("bad argument type", 
				Stream.of(types).map(Object::toString).collect(joining("|")), actual), e);
	}

	public static BadArgumentException badArgumentCountException(int count, int actual) {
		return new BadArgumentException(formatMessage("bad argument count", count, actual));
	}

	public static BadArgumentException badArgumentsException(String exp, String actual, Exception e) {
		return new BadArgumentException(formatMessage("bad arguments", exp, actual), e);
	}
}
