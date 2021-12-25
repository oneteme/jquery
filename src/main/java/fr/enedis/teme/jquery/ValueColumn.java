package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.toSqlString;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueColumn<T> implements DBColumn {

	@Getter
	private final String mappedName;
	private final T value; //nullable

	@Override
	public String sql(DBTable table) {//see count(*) 
		return "*".equals(value) ? value.toString() : toSqlString(value);
	}
	
	@Override
	public String tag(DBTable table) {
		return mappedName;
	}
	
	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public String toString() {
		return sql(null);
	}
	
	public static <T> ValueColumn<T> staticColumn(@NonNull String mappedName, T expression) {
		return new ValueColumn<>(requireNonBlank(mappedName), expression);
	}
}
