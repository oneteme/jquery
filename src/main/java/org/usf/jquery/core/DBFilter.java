package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBObject, Nested, Chainable<DBFilter> {
	
	String sql(QueryVariables builder);

	DBFilter append(LogicalOperator op, DBFilter filter);

	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, DBFilter.class::getSimpleName);
		return sql(builder);
	}
}