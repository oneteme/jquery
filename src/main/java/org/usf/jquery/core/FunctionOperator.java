package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.isPresent;

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
				.append("(")
				.appendIf(isPresent(args), ()-> range(0, args.length)
						.mapToObj(i-> builder.appendLitteral(args[i]))
						.collect(joining(SCOMA))) //accept any
				.append(")")
				.toString();
	}
}
