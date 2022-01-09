package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;

public interface OperatorExpression extends DBExpression<String> {

	OperatorExpression append(LogicalOperator op, OperatorExpression exp);
	
	default OperatorExpression and(OperatorExpression exp) {
		return append(AND, exp);
	}

	default OperatorExpression or(OperatorExpression exp) {
		return append(OR, exp);
	}

}
