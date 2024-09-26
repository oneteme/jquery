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
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		sql(sb, ctx, args, 0);
	}
	
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args, int from) {
		sb.function(id(), ()-> ctx.appendLiteralArray(sb, args, from)); //avoid sub array
	}
}
