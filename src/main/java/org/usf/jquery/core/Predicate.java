package org.usf.jquery.core;

import static org.usf.jquery.core.Stores.getCurrentDialect;
import static org.usf.jquery.core.Validation.requireNArgs;

import lombok.NonNull;
/**
 * 
 * @author u$f
 *
 */
public interface Predicate extends QueryPart, Chainable<Predicate> {

	void build(SqlBuilder builder, Object left); // do change method order
	
	@Override
	default void build(SqlBuilder builder, Object... args) {
		requireNArgs(1, args, Predicate.class::getSimpleName);
		build(builder, args[0]);
	}
	
	static Predicate eq(Object right) {
		return getCurrentDialect().eq().invokeAsExpression(right);
	}

	static Predicate ne(Object right) {
		return getCurrentDialect().ne().invokeAsExpression(right);
	}
	
	static Predicate lt(Object right) {
		return getCurrentDialect().lt().invokeAsExpression(right);
	}

	static Predicate le(Object right) {
		return getCurrentDialect().le().invokeAsExpression(right);
	}

	static Predicate gt(Object right) {
		return getCurrentDialect().gt().invokeAsExpression(right);
	}

	static Predicate ge(Object right) {
		return getCurrentDialect().ge().invokeAsExpression(right);
	}
	
	static Predicate like(Object right) {
		return getCurrentDialect().like().invokeAsExpression(right);
	}
	
	static Predicate iLike(Object right) {
		return getCurrentDialect().iLike().invokeAsExpression(right);
	}

	static Predicate notLike(Object right) {
		return getCurrentDialect().notLike().invokeAsExpression(right);
	}

	static Predicate notILike(Object right) {
		return getCurrentDialect().notILike().invokeAsExpression(right);
	}

	static Predicate isNull() {
		return getCurrentDialect().isNull().invokeAsExpression();
	}

	static Predicate isNotNull() {
		return getCurrentDialect().notNull().invokeAsExpression();
	}

	@SuppressWarnings("unchecked")
	static <T> Predicate in(@NonNull T... right) {
		return getCurrentDialect().in().invokeAsExpression((Object[])right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> Predicate notIn(@NonNull T... right) {
		return getCurrentDialect().notIn().invokeAsExpression((Object[])right);
	}
	
}
