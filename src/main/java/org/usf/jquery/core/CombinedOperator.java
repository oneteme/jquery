package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface CombinedOperator extends Operator {
	
	@Override
	OperationColumn args(JDBCType type, Object... args);

	@Override
	default String id() {
		return null;
	}
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		throw new UnsupportedOperationException("sql");
	}
}
