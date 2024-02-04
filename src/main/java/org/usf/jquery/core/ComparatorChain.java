package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

public interface ComparatorChain extends Comparator {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		throw new UnsupportedOperationException("no sql");
	}
	
	@Override
	default DBFilter args(Object... args) {
		requireNArgs(2, args, this::id);
		return ((DBFilter)args[0]).append(LogicalOperator.valueOf(id()), ((DBFilter)args[1]));
	}
	
}
