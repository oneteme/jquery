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
		return new BadArgumentException("bad argument type, "+ 
				"expected: " + Stream.of(types).map(Object::toString).collect(joining("|")) + 
				" but was: " + actual, e);
	}

	public static BadArgumentException badArgumentCountException(int count, int actual) {
		return new BadArgumentException("bad argument count, "+ 
				"expected: " + count + 
				" but was: " + actual);
	}
}
