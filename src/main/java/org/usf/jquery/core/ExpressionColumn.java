package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.isColumnAggregation;
import static org.usf.jquery.core.DBColumn.isColumnConstant;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ExpressionColumn implements DBColumn {
	
	@NonNull
	private final Object operand;
	@NonNull
	private final OperationSingleExpression operator;
	
	@Override
	public String sql(QueryParameterBuilder arg) {
		return operator.sql(arg, operand);
	}
	
	@Override
	public boolean isAggregation() {
		return isColumnAggregation(operand) ||
				isColumnAggregation(operator.getValue());
	}
	
	@Override
	public boolean isConstant() {
		return isColumnConstant(operand) && 
				isColumnConstant(operator.getValue());
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
}
