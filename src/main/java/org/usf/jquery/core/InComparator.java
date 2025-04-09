package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface InComparator extends Comparator {

	@Override
	default void build(QueryBuilder query, Object... args) {
		requireAtLeastNArgs(2, args, InComparator.class::getSimpleName);
		query.appendParameter(args[0]).appendSpace()
		.append(id()).appendParameters(SCOMA, args, 1);
	}
}
