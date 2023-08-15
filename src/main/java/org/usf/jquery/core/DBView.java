package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireAtMostNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireAtMostNArgs(1, args, DBView.class::getSimpleName);
		return sql(builder, isEmpty(args) ? null : args[0].toString());
	}

	String sql(QueryParameterBuilder builder, String schema);
	
}
