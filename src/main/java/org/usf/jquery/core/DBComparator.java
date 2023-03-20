package org.usf.jquery.core;

import static org.usf.jquery.core.InCompartor.inComparator;
import static org.usf.jquery.core.NullComparator.nullComparator;
import static org.usf.jquery.core.StringComparator.stringComparator;
import static org.usf.jquery.core.BasicComparator.basicComparator;

import lombok.NonNull;

@FunctionalInterface
public interface DBComparator extends DBCallable {
	
	default ComparatorExpression args(Object right) {
		return new ComparisonSingleExpression(this, right);
	}
	
	static ComparatorExpression equal(Object right) {
		return basicComparator("=").args(right);
	}

	static ComparatorExpression notEqual(Object right) {
		return basicComparator("<>").args(right);
	}
	
	static ComparatorExpression lessThan(Object right) {
		return basicComparator("<").args(right);
	}

	static ComparatorExpression lessOrEqual(Object right) {
		return basicComparator("<=").args(right);
	}

	static ComparatorExpression greaterThan(Object right) {
		return basicComparator(">").args(right);
	}

	static ComparatorExpression greaterOrEqual(Object right) {
		return basicComparator(">=").args(right);
	}
	
	static ComparatorExpression like(Object right) {
		return stringComparator("LIKE").args(right);
	}
	
	static ComparatorExpression iLike(Object right) {
		return stringComparator("ILIKE").args(right);
	}

	static ComparatorExpression notLike(Object right) {
		return stringComparator("NOT LIKE").args(right);
	}

	static ComparatorExpression notILike(Object right) {
		return stringComparator("NOT ILIKE").args(right);
	}

	static ComparatorExpression isNull() {
		return nullComparator("IS NULL").args(null);
	}

	static ComparatorExpression isNotNull() {
		return nullComparator("IS NOT NULL").args(null);
	}

	@SuppressWarnings("unchecked")
	static <T> ComparatorExpression in(@NonNull T... right) {
		return inComparator("IN").args(right);
	}
	
	@SuppressWarnings("unchecked")
	static <T> ComparatorExpression notIn(@NonNull T... right) {
		return inComparator("NOT IN").args(right);
	}

}
