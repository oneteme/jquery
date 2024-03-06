package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBFilter extends DBObject, Aggregable, Chainable<DBFilter> {
	
	String sql(QueryParameterBuilder builder);

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBFilter.class::getSimpleName);
		return sql(builder);
	}
	 
	default DBFilter append(LogicalOperator op, DBFilter filter) {
		throw new UnsupportedOperationException(); //explicitly overridden
	}
}