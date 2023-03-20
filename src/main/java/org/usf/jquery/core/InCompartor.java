package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.util.stream.Stream;

@FunctionalInterface
public interface InCompartor extends DBComparator {

	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, s-> s>1), ()-> name() + " compartor takes at least 2 parameters");
		var params = Stream.of(args).skip(1).toArray();
		return builder.appendParameter(args[0]) + SPACE + name() + parenthese(builder.appendArray(params));
	}
	
	static InCompartor inComparator(final String name) {
		return ()-> name;
	}
}
