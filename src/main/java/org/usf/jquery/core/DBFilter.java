package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBObject, Nested, Chainable<DBFilter> {
	
	String sql(QueryVariables builder);

	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, DBFilter.class::getSimpleName);
		return sql(builder);
	}
}