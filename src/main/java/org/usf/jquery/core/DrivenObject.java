package org.usf.jquery.core;

import java.util.function.BinaryOperator;

/**
 * 
 * @author u$f
 *
 *
 */
@FunctionalInterface
public interface DrivenObject<T> {
	
	T adjust(QueryBuilder query, T value);
	
	static ValueColumn drivenConstant(JDBCType type, Object value, BinaryOperator<Object> fn) {
		return new ValueColumn(type, value) {
			@Override
			public Object adjust(QueryBuilder query, Object value) {
				return fn.apply(query.getCurrentModel(), value);
			}
		};
	}
}
