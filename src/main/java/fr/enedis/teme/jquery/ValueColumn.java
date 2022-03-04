package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.formatValue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueColumn<T> implements DBColumn {

	private final T value; //nullable

	@Override
	public String sql(DBTable table, QueryParameterBuilder ph) {//see count(*) 
		return formatValue(value);
	}
	
	@Override
	public boolean isAggregation() {
		return false;
	}
	
	@Override
	public boolean isExpression() {
		return false;
	}
	
	@Override
	public boolean isConstant() {
		return true;
	}
	
	@Override
	public String toString() {
		return sql(null, null);
	}
	
	public static <T> ValueColumn<T> staticColumn(T expression) {
		return new ValueColumn<>(expression);
	}
}
