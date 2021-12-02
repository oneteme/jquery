package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import lombok.Getter;

@Getter
public final class ExpressionColumn implements Column {
	
	private final String expression;
	private final String mappedName;

	public ExpressionColumn(String expression, String mappedName) {
		this.expression = requireNonNull(expression);
		this.mappedName = requireNonNull(mappedName);
		if(expression.contains(",")) {
			throw new IllegalArgumentException("multi columns");
		}
	}

	@Override
	public String toSql(Table table) {
		return expression;
	}

	@Override
	public String toString() {
		return toSql(null) + " AS " + mappedName;
	}

}
