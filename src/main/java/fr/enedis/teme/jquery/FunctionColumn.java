package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBColumn.isColumnAggregation;
import static fr.enedis.teme.jquery.DBColumn.isColumnConstant;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class FunctionColumn implements DBColumn {

	@NonNull
	private final DBFunction function;
	@NonNull
	private final Object operand;
	private final Object value; //nullable

	public FunctionColumn(DBFunction function, Object operand) {
		this(function, operand, null);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return DBCallable.sql(function, builder, operand, value);
	}

	@Override
	public boolean isAggregation() {
		return function.isAggregate() || isColumnAggregation(operand);
	}

	@Override
	public boolean isConstant() {
		return isColumnConstant(operand);
	}

	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
