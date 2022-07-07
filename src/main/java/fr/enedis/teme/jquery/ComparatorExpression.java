package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;

public interface ComparatorExpression extends DBExpression {
	
	ComparatorExpression append(LogicalOperator op, ComparatorExpression exp);
	
	default ComparatorExpression and(ComparatorExpression exp) {
		return append(AND, exp);
	}

	default ComparatorExpression or(ComparatorExpression exp) {
		return append(OR, exp);
	}

}
