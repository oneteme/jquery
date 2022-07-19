package org.usf.jquery;

import static org.usf.jquery.LogicalOperator.AND;
import static org.usf.jquery.LogicalOperator.OR;

public interface ComparatorExpression extends DBExpression {
	
	ComparatorExpression append(LogicalOperator op, ComparatorExpression exp);
	
	default ComparatorExpression and(ComparatorExpression exp) {
		return append(AND, exp);
	}

	default ComparatorExpression or(ComparatorExpression exp) {
		return append(OR, exp);
	}

}
