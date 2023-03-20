package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

public interface ComparatorExpression extends DBExpression {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, 1), ()-> "DBFilter takes no arguments");
		return sql(builder, args[0]);
	}

	String sql(QueryParameterBuilder builder, Object args); // do change method order
	
	ComparatorExpression append(LogicalOperator op, ComparatorExpression exp);
	
	default ComparatorExpression and(ComparatorExpression exp) {
		return append(AND, exp);
	}

	default ComparatorExpression or(ComparatorExpression exp) {
		return append(OR, exp);
	}

}
