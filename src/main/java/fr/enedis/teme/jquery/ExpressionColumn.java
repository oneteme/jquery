package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ExpressionColumn implements DBColumn {
	
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
		return operand instanceof DBColumn && ((DBColumn)operand).isAggregation();
	}
	
	@Override
	public boolean isConstant() {
		return operand instanceof DBColumn && ((DBColumn)operand).isConstant();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
}
