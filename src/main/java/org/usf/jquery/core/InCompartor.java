package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

@FunctionalInterface
public interface InCompartor extends DBComparator {

	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, s-> s>1), ()-> name() + " comparator takes at least 2 parameters");
		var params = copyOfRange(args, 1, args.length);
		return builder.appendParameter(args[0]) + SPACE + name() + parenthese(builder.appendArray(params));
	}
	
	static InCompartor inComparator(final String name) {
		return ()-> name;
	}
}
