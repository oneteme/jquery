package org.usf.jquery.core;

import static org.usf.jquery.core.Dialect.getDialect;
import static org.usf.jquery.core.Validation.requireNArgs;

import lombok.NonNull;
/**
 * 
 * @author u$f
 *
 */
public interface Predicate extends DBObject, Chainable<Predicate> {

	void build(QueryBuilder query, Object left); // do change method order

	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(1, args, Predicate.class::getSimpleName);
		build(query, args[0]);
	}
	
	static Predicate eq(Object right) {
		return getDialect().eq().invokeAsExpression(right);
	}

	static Predicate ne(Object right) {
		return getDialect().ne().invokeAsExpression(right);
	}
	
	static Predicate lt(Object right) {
		return getDialect().lt().invokeAsExpression(right);
	}

	static Predicate le(Object right) {
		return getDialect().le().invokeAsExpression(right);
	}

	static Predicate gt(Object right) {
		return getDialect().gt().invokeAsExpression(right);
	}

	static Predicate ge(Object right) {
		return getDialect().ge().invokeAsExpression(right);
	}
	
	static Predicate like(Object right) {
		return getDialect().like().invokeAsExpression(right);
	}
	
	static Predicate iLike(Object right) {
		return getDialect().iLike().invokeAsExpression(right);
	}

	static Predicate notLike(Object right) {
		return getDialect().notLike().invokeAsExpression(right);
	}

	static Predicate notILike(Object right) {
		return getDialect().notILike().invokeAsExpression(right);
	}

	static Predicate isNull() {
		return getDialect().isNull().invokeAsExpression();
	}

	static Predicate isNotNull() {
		return getDialect().notNull().invokeAsExpression();
	}

	@SuppressWarnings("unchecked")
	static <T> Predicate in(@NonNull T... right) {
		return getDialect().in().invokeAsExpression((Object[])right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> Predicate notIn(@NonNull T... right) {
		return getDialect().notIn().invokeAsExpression((Object[])right);
	}
	
}
