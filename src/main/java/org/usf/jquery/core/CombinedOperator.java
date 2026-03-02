package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CombinedOperator extends Operator {
	
	Column combine(Object... args);
	
	@Override
	default Column operation(JDBCType type, Object... args) {
		var c = combine(args);
		if(type == c.getType()) {
			return c;
		}
		throw new IllegalStateException("type mismatch : " + type);
	}

	@Override
	default String id() {
		return "CombinedOperator"; //try get id
	}
	
	@Override
	default void buildOperator(QueryBuilder query, Object... args) { //no SQL
		throw new UnsupportedOperationException("CombinedOperator::sql");
	}
}
