package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.function.Function;

/**
 * 
 * @author u$f
 *
 */
interface ArgTypeRef extends Function<Object[], JDBCType> {

	static ArgTypeRef firstArgJdbcType() {
		return arr-> typeOf(requireAtLeastNArgs(1, arr, ArgTypeRef.class::getSimpleName)[0])
				.orElse(null); // not sure 
	} 
}