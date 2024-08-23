package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.joinArray;

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
		String type = actual instanceof DBColumn c ? ":"+ c.getType() : "";
		return new BadArgumentException(formatMessage("bad argument type", joinArray("|", types), actual + type));
	}

	public static BadArgumentException badArgumentCountException(int count, int actual) {
		return new BadArgumentException(formatMessage("bad argument count", count, actual));
	}

	public static BadArgumentException badArgumentsException(String exp, String actual, Exception e) {
		return new BadArgumentException(formatMessage("bad arguments", exp, actual), e);
	}
}
