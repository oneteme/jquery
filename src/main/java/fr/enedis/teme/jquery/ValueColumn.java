package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.sqlString;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueColumn<T> implements DBColumn {
	
	private final String mappedName;
	private final T expression; //nullable

	@Override
	public String toSql(DBTable table) {
		if(expression == null) {
			return "null";
		}
		if(expression instanceof Number || "*".equals(expression)) {
			return expression.toString();
		}
		return sqlString(expression.toString());
	}
	
	@Override
	public String sqlAlias(DBTable table) {
		return mappedName;
	}
	
	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public String toString() {
		return toSql(null);
	}
	
	public static <T> ValueColumn<T> staticColumn(@NonNull String mappedName, T expression) {
		return new ValueColumn<>(requireNonBlank(mappedName), expression);
	}
}
