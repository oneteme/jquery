package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.sqlString;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;
import static java.util.Objects.requireNonNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstantColumn<T> implements DBColumn {
	
	@NonNull
	private final T expression;
	@NonNull
	private final String mappedName;

	@Override
	public String toSql(DBTable table) {
		return expression instanceof Number 
			? expression.toString() 
			: sqlString(expression.toString());
	}
	
	@Override
	public String getAlias(DBTable table) {
		return mappedName;
	}

	@Override
	public String toString() {
		return toSql(null);
	}
	
	public static <T> ConstantColumn<T> staticColumn(T expression, String mappedName) {
		return new ConstantColumn<>(
				requireNonNull(expression), 
				requireNonBlank(mappedName));
	}
}
