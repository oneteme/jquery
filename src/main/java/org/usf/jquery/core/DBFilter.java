package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBFilter extends DBObject, NestedSql {
	
	String sql(QueryParameterBuilder builder);

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, ()-> "DBFilter");
		return sql(builder);
	}

	//explicitly overridden 
	default DBFilter append(LogicalOperator op, DBFilter filter) {
		throw new UnsupportedOperationException();
	}

	default DBFilter and(DBFilter filter) {
		return append(AND, filter);
	}

	default DBFilter or(DBFilter filter) {
		return append(OR, filter);
	}
}
