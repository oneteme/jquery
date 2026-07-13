package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 
 * @author u$f
 *
 */
public final record TypedArg(Object value, JDBCType type) {
	
	public static TypedArg arg(Object value, JDBCType type) {
		return new TypedArg(value, type);
	}	
	
	@Override
	public final String toString() {
		var s = isNull(value) ? "null" : value.toString();
		if(nonNull(type)) {
			s+=":"+type.name();
		}
		return s;
	}
}
