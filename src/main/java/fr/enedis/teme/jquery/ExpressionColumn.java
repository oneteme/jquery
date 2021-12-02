package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.illegalArgumentIf;
import static java.util.Objects.requireNonNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionColumn<T> implements DBColumn {
	
	private final T expression;
	private final String mappedName;
	private final boolean _static;

	@Override
	public String toSql(DBTable table) {
		if(!_static || expression instanceof Number) {
			return expression.toString();
		}
		return "'" + expression.toString() + "'";
	}

	@Override
	public String toString() {
		return toSql(null) + " AS " + mappedName;
	}
	
	public static ExpressionColumn<String> expressionColumn(String expression, String mappedName) {
		illegalArgumentIf(requireNonNull(expression).contains(","), //TD use regex
				()-> "expression contains multi columns : " + expression);
		return new ExpressionColumn<>(expression, requireNonNull(mappedName), false);
	}
	
	public static <T> ExpressionColumn<T> staticColumn(T expression, String mappedName) {
		return new ExpressionColumn<>(requireNonNull(expression), requireNonNull(mappedName), true);
	}

}
