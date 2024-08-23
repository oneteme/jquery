package org.usf.jquery.core;

/**
 * https://learnsql.com/blog/standard-sql-functions-cheat-sheet/
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface FunctionOperator extends Operator {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		return new SqlStringBuilder(id())
				.append("(").append(builder.appendLiteralArray(args)).append(")") //accept any
				.toString();
	}
}
