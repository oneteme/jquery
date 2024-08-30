package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CombinedOperator extends Operator {
	
	OperationColumn args(Object... args);
	
	@Override
	default OperationColumn args(JDBCType type, Object... args) {
		var c = args(args);
		if(type == c.getType()) {
			return c;
		}
		throw new IllegalStateException("type mismatch : " + type);
	}

	@Override
	default String id() {
		return null;
	}
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		throw new UnsupportedOperationException("CombinedOperator::sql");
	}
}
