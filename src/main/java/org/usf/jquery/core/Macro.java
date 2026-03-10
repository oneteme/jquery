package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Macro extends Invocable<Column> {
	
	Column apply(Object... args);
	
	@Override
	default Column invoke(JavaType type, Object... args) {
		var col = apply(args);
		if(type == col.getType()) {
			return col;
		}
		throw new IllegalStateException("invalid type " + type + " for " + col);
	}
}