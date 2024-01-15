package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.space;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface StringComparator extends Comparator {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, StringComparator.class::getSimpleName);
		var type = typeOf(args[0]).orElseThrow(Comparator::typeCannotBeNullException); // null 'cmp'
		return builder.appendLitteral(args[0]) + space(id()) + builder.appendParameter(type, args[1]);
	}
}
