package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBObject, NestedSql {
	
	String sql(QueryParameterBuilder builder);

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(nonNull(args), "DBFilter takes no arguments");
		return sql(builder);
	}

	DBFilter append(LogicalOperator op, DBFilter filter);

	default DBFilter and(DBFilter filter) {
		return append(AND, filter);
	}

	default DBFilter or(DBFilter filter) {
		return append(OR, filter);
	}
}
