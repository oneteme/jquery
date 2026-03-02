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
		return Comparator.eq().expression(right);
	}

	static Predicate ne(Object right) {
		return Comparator.ne().expression(right);
	}
	
	static Predicate lt(Object right) {
		return Comparator.lt().expression(right);
	}

	static Predicate le(Object right) {
		return Comparator.le().expression(right);
	}

	static Predicate gt(Object right) {
		return Comparator.gt().expression(right);
	}

	static Predicate ge(Object right) {
		return Comparator.ge().expression(right);
	}
	
	static Predicate like(Object right) {
		return Comparator.like().expression(right);
	}
	
	static Predicate iLike(Object right) {
		return Comparator.iLike().expression(right);
	}

	static Predicate notLike(Object right) {
		return Comparator.notLike().expression(right);
	}

	static Predicate notILike(Object right) {
		return Comparator.notILike().expression(right);
	}

	static Predicate isNull() {
		return Comparator.isNull().expression();
	}

	static Predicate isNotNull() {
		return Comparator.notNull().expression();
	}

	@SuppressWarnings("unchecked")
	static <T> Predicate in(@NonNull T... right) {
		return Comparator.in().expression((Object[])right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> Predicate notIn(@NonNull T... right) {
		return Comparator.notIn().expression((Object[])right);
	}
}
