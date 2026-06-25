package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public record TypedArg(Object value, JDBCType type) {
	
	public static TypedArg arg(Object value, JDBCType type) {
		return new TypedArg(value, type);
	}	
}
