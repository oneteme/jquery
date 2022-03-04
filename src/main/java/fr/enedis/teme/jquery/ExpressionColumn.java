package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExpressionColumn implements DBColumn {
	
	@NonNull
	private final DBColumn leftOp;
	@NonNull
	private final DBColumn rigthOp;
	@NonNull
	private final ArithmeticOperator operator;
	
	@Override
	public String sql(DBTable obj, QueryParameterBuilder arg) {
		
		return leftOp.sql(obj, arg) + operator.sql() + rigthOp.sql(obj, arg);
	}
	
	@Override
	public boolean isAggregation() {
		return leftOp.isAggregation() || rigthOp.isAggregation();
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}

	@Override
	public boolean isConstant() {
		return leftOp.isConstant() && rigthOp.isConstant();
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
}
