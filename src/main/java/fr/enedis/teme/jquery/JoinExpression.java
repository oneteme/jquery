package fr.enedis.teme.jquery;

import java.util.function.Function;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class JoinExpression {
	
	private final DBTable leftTable;
	private final DBColumn leftColumn;
	private final CompareOperator operator;
	private final DBTable rightTable;
	private final DBColumn rightColumn;


	public String sql(Function<DBTable, String> fn, QueryParameterBuilder arg) {
		
		var sb = new StringBuilder(50)
				.append(fn.apply(leftTable) + "." + leftColumn.sql(leftTable, arg));
		return (rightColumn == null 
				? sb.append(operator.sql(null, arg)) 
				: sb.append(fn.apply(rightTable) + "." + operator.sql(rightColumn.sql(rightTable, arg), arg)))
				.toString();
	}
	
}
