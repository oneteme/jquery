package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBObject, Nested, Chainable<DBFilter> {
	
	void sql(SqlStringBuilder sb, QueryContext ctx);

	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBFilter.class::getSimpleName);
		sql(sb, ctx);
	}
}