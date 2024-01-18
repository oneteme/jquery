package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public interface ComparisonExpression extends DBExpression, NestedSql, Chainable<ComparisonExpression> {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(1, args, ComparisonExpression.class::getSimpleName);
		return sql(builder, args[0]);
	}

	String sql(QueryParameterBuilder builder, Object left); // do change method order
	
	static ComparisonExpression equal(Object right) {
		return Comparator.eq().expression(right);
	}

	static ComparisonExpression notEqual(Object right) {
		return Comparator.ne().expression(right);
	}
	
	static ComparisonExpression lessThan(Object right) {
		return Comparator.lt().expression(right);
	}

	static ComparisonExpression lessOrEqual(Object right) {
		return Comparator.le().expression(right);
	}

	static ComparisonExpression greaterThan(Object right) {
		return Comparator.gt().expression(right);
	}

	static ComparisonExpression greaterOrEqual(Object right) {
		return Comparator.ge().expression(right);
	}
	
	static ComparisonExpression like(Object right) {
		return Comparator.like().expression(right);
	}
	
	static ComparisonExpression iLike(Object right) {
		return Comparator.iLike().expression(right);
	}

	static ComparisonExpression notLike(Object right) {
		return Comparator.notLike().expression(right);
	}

	static ComparisonExpression notILike(Object right) {
		return Comparator.notILike().expression(right);
	}

	static ComparisonExpression isNull() {
		return Comparator.isNull().expression(null);
	}

	static ComparisonExpression isNotNull() {
		return Comparator.notNull().expression(null);
	}

	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression in(@NonNull T... right) {
		return Comparator.in().expression(right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression notIn(@NonNull T... right) {
		return Comparator.notIn().expression(right);
	}

}
