package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ClauseFunction extends FunctionOperator {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		return isEmpty(args) 
				? EMPTY 
				: id() + SPACE + Stream.of(args).map(builder::appendParameter).collect(joining(COMA));
	}
	
}
