package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface InCompartor extends Comparator {

	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireAtLeastNArgs(2, args, InCompartor.class::getSimpleName);
		var params = copyOfRange(args, 1, args.length);
		return builder.appendLitteral(args[0]) + SPACE + name() + parenthese(builder.appendArrayParameter(params));
	}
}
