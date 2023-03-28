package org.usf.jquery.core;

import static org.usf.jquery.core.BasicComparator.basicComparator;
import static org.usf.jquery.core.InCompartor.inComparator;
import static org.usf.jquery.core.NullComparator.nullComparator;
import static org.usf.jquery.core.StringComparator.stringComparator;

import lombok.NonNull;

@FunctionalInterface
public interface DBComparator extends DBCallable {
	
	default ComparisonExpression args(Object right) {
		return new ComparisonSingleExpression(this, right);
	}
	
	static ComparisonExpression equal(Object right) {
		return basicComparator("=").args(right);
	}

	static ComparisonExpression notEqual(Object right) {
		return basicComparator("<>").args(right);
	}
	
	static ComparisonExpression lessThan(Object right) {
		return basicComparator("<").args(right);
	}

	static ComparisonExpression lessOrEqual(Object right) {
		return basicComparator("<=").args(right);
	}

	static ComparisonExpression greaterThan(Object right) {
		return basicComparator(">").args(right);
	}

	static ComparisonExpression greaterOrEqual(Object right) {
		return basicComparator(">=").args(right);
	}
	
	static ComparisonExpression like(Object right) {
		return stringComparator("LIKE").args(right);
	}
	
	static ComparisonExpression iLike(Object right) {
		return stringComparator("ILIKE").args(right);
	}

	static ComparisonExpression notLike(Object right) {
		return stringComparator("NOT LIKE").args(right);
	}

	static ComparisonExpression notILike(Object right) {
		return stringComparator("NOT ILIKE").args(right);
	}

	static ComparisonExpression isNull() {
		return nullComparator("IS NULL").args(null);
	}

	static ComparisonExpression isNotNull() {
		return nullComparator("IS NOT NULL").args(null);
	}

	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression in(@NonNull T... right) {
		return inComparator("IN").args(right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> ComparisonExpression notIn(@NonNull T... right) {
		return inComparator("NOT IN").args(right);
	}

}
