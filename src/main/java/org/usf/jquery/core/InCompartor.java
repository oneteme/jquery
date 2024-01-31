package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.JDBCType.typeOf;
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

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireAtLeastNArgs(2, args, InCompartor.class::getSimpleName);
		var type = typeOf(args[0]).orElseThrow(Comparator::typeCannotBeNullException);
		var varg = copyOfRange(args, 1, args.length);
		return builder.appendLitteral(args[0]) + SPACE + id() + parenthese(args.length == 2 && args[1] instanceof InternalQuery ? builder.appendLitteral(args[1]) : builder.appendArrayParameter(type, varg));
	}
}
