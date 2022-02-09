package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.formatValue;
import static fr.enedis.teme.jquery.Validation.requireLegalVariable;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueColumn<T> implements TaggableColumn {

	private final String tagName;
	private final T value; //nullable

	@Override
	public String sql(DBTable table, QueryParameterBuilder ph) {//see count(*) 
		return "*".equals(value) ? value.toString() : formatValue(value);
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
	public String tagname() {
		return tagName;
	}

	@Override
	public String toString() {
		return sql(null, null);
	}
	
	public static <T> ValueColumn<T> staticColumn(@NonNull String tagName, T expression) {
		return new ValueColumn<>(requireLegalVariable(tagName), expression);
	}
}
