package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.function.Function;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface TypeResolver extends Function<Object[], JavaType> {

	static TypeResolver firstArgType() {
		return arr-> typeOf(requireAtLeastNArgs(1, arr, TypeResolver.class::getSimpleName)[0]).orElse(null);
	}
	
	static TypeResolver argTypeAt(int index) {
		return arr-> typeOf(requireAtLeastNArgs(index, arr, TypeResolver.class::getSimpleName)[index]).orElse(null);
	}
}