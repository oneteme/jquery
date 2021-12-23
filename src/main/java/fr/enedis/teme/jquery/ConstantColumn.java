package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.sqlString;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstantColumn<T> implements DBColumn {
	
	@NonNull
	private final String mappedName;
	private final T expression; //nullable

	@Override
	public String toSql(DBTable table) {
		if(expression == null) {
			return "null";
		}
		if(expression instanceof Number) {
			return expression.toString();
		}
		return "*".equals(expression) 
			? expression.toString()
			: sqlString(expression.toString());
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}
	
	@Override
	public boolean isConstant() {
		return true;
	}
	
	@Override
	public String getAlias(DBTable table) {
		return mappedName;
	}

	@Override
	public String toString() {
		return toSql(null);
	}
	
	public static <T> ConstantColumn<T> staticColumn(String mappedName, T expression) {
		return new ConstantColumn<>(requireNonBlank(mappedName), expression);
	}
}
