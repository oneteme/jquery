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
	default String sql(QueryContext ctx, Object[] args) {
		return new SqlStringBuilder(id())
				.append("(").append(ctx.appendLiteralArray(args)).append(")") //accept any
				.toString();
	}
}
