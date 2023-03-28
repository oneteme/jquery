package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

public interface ComparisonExpression extends DBExpression, NestedSql {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, 1), "comparison takes one argument");
		return sql(builder, args[0]);
	}

	String sql(QueryParameterBuilder builder, Object left); // do change method order
	
	ComparisonExpression append(LogicalOperator op, ComparisonExpression exp);
	
	default ComparisonExpression and(ComparisonExpression exp) {
		return append(AND, exp);
	}

	default ComparisonExpression or(ComparisonExpression exp) {
		return append(OR, exp);
	}

}
