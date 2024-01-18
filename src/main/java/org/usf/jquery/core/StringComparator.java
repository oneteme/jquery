package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.VARCHAR;
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
		return builder.appendLitteral(args[0]) + space(id()) + builder.appendParameter(VARCHAR, args[1]);
	}
}
