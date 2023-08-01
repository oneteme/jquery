package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		return sql(builder);
	}

	String sql(QueryParameterBuilder builder);
	
}
