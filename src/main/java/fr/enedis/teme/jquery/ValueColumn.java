package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.formatValue;
import static fr.enedis.teme.jquery.Validation.requireLegalVariable;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueColumn<T> implements DBColumn {

	@Getter
	private final String tagName;
	private final T value;//nullable

	@Override
	public String sql(DBTable table, ParameterHolder ph) {//see count(*) 
		return "*".equals(value) ? value.toString() : formatValue(value);
	}
	
	@Override
	public String tag(DBTable table) {
		return tagName;
	}
	
	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public String toString() {
		return sql(null, null);
	}
	
	public static <T> ValueColumn<T> staticColumn(@NonNull String tagName, T expression) {
		return new ValueColumn<>(requireLegalVariable(tagName), expression); //requireLegalVariable
	}
}
