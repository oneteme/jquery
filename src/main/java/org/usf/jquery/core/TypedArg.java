package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

/**
 * 
 * @author u$f
 *
 */
public record TypedArg(Object value, JDBCType type) {
	
	public static TypedArg arg(Object value, JDBCType type) {
		return new TypedArg(value, type);
	}
	
	public static Object[] values(TypedArg... arr) {
		return nonNull(arr) ? stream(arr).map(TypedArg::value).toArray() : null;
	}
}
