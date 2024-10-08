package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public interface ComparisonExpression extends DBObject, Nested, Chainable<ComparisonExpression> {

	void sql(SqlStringBuilder sb, QueryContext ctx, Object left); // do change method order

	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNArgs(1, args, ComparisonExpression.class::getSimpleName);
		sql(sb, ctx, args[0]);
	}
	
	static ComparisonExpression eq(Object right) {
		return Comparator.eq().expression(right);
	}

	static ComparisonExpression ne(Object right) {
		return Comparator.ne().expression(right);
	}
	
	static ComparisonExpression lt(Object right) {
		return Comparator.lt().expression(right);
	}

	static ComparisonExpression le(Object right) {
		return Comparator.le().expression(right);
	}

	static ComparisonExpression gt(Object right) {
		return Comparator.gt().expression(right);
	}

	static ComparisonExpression ge(Object right) {
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
		return Comparator.isNull().expression();
	}

	static ComparisonExpression isNotNull() {
		return Comparator.notNull().expression();
	}

	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression in(@NonNull T... right) {
		return Comparator.in().expression((Object[])right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression notIn(@NonNull T... right) {
		return Comparator.notIn().expression((Object[])right);
	}
}
