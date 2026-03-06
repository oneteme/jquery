package org.usf.jquery.core;

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
		return Comparator.eq().applyAsExpression(right);
	}

	static Predicate ne(Object right) {
		return Comparator.ne().applyAsExpression(right);
	}
	
	static Predicate lt(Object right) {
		return Comparator.lt().applyAsExpression(right);
	}

	static Predicate le(Object right) {
		return Comparator.le().applyAsExpression(right);
	}

	static Predicate gt(Object right) {
		return Comparator.gt().applyAsExpression(right);
	}

	static Predicate ge(Object right) {
		return Comparator.ge().applyAsExpression(right);
	}
	
	static Predicate like(Object right) {
		return Comparator.like().applyAsExpression(right);
	}
	
	static Predicate iLike(Object right) {
		return Comparator.iLike().applyAsExpression(right);
	}

	static Predicate notLike(Object right) {
		return Comparator.notLike().applyAsExpression(right);
	}

	static Predicate notILike(Object right) {
		return Comparator.notILike().applyAsExpression(right);
	}

	static Predicate isNull() {
		return Comparator.isNull().applyAsExpression();
	}

	static Predicate isNotNull() {
		return Comparator.notNull().applyAsExpression();
	}

	@SuppressWarnings("unchecked")
	static <T> Predicate in(@NonNull T... right) {
		return Comparator.in().applyAsExpression((Object[])right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> Predicate notIn(@NonNull T... right) {
		return Comparator.notIn().applyAsExpression((Object[])right);
	}
}
