package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface BasicComparator extends Comparator {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, BasicComparator.class::getSimpleName);
		var type = typeOf(args[0])
				.or(()-> typeOf(args[1]))
				.orElseThrow(Comparator::typeCannotBeNullException); // null 'cmp' null
		return builder.appendLitteral(args[0]) + id() + builder.appendParameter(type, args[1]);
	}
}