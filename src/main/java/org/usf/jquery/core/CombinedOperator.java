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
	default OperationColumn operation(JDBCType type, Object... args) {
		var c = args(args);
		if(type == c.getType()) {
			return c;
		}
		throw new IllegalStateException("type mismatch : " + type);
	}

	@Override
	default String id() {
		return "CombinedOperator"; //do better
	}
	
	@Override
	default String sql(QueryContext ctx, Object[] args) { //no SQL
		throw new UnsupportedOperationException("CombinedOperator::sql");
	}
}
