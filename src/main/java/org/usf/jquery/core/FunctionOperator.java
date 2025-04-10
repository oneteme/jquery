package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SCOMA;

/**
 * https://learnsql.com/blog/standard-sql-functions-cheat-sheet/
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface FunctionOperator extends Operator {

	@Override
	default void build(QueryBuilder query, Object... args) {
		sql(query, args, 0);
	}
	
	default void sql(QueryBuilder query, Object[] args, int from) {
		query.append(id()).appendLiteral(SCOMA, args, from); //avoid sub array
	}
}
