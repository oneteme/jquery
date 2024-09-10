package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBObject, Nested, Chainable<DBFilter> {
	
	String sql(QueryContext ctx);

	@Override
	default String sql(QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBFilter.class::getSimpleName);
		return sql(ctx);
	}
}