package org.usf.jquery.core;

public interface ComparatorPipe extends Comparator {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		throw new UnsupportedOperationException("no sql");
	}
	
	@Override
	default DBFilter args(Object... args) {
		return ((DBFilter)args[0]).append(LogicalOperator.valueOf(id()), ((DBFilter)args[1]));
	}
	
}
