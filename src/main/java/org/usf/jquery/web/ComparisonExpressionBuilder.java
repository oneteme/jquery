package org.usf.jquery.web;

import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.LogicalOperator;

/**
 * 
 * @author u$f
 * 
 * @see ComparisonExpression
 *
 */
@FunctionalInterface
public interface ComparisonExpressionBuilder<T> {
	
	ComparisonExpression expression(T arg);
	
	default LogicalOperator joiner() {
		return OR;
	}
	
	@SuppressWarnings("unchecked")
	default ComparisonExpression build(T... args) {
		return Stream.of(requireAtLeastNArgs(1, args, ()-> "pretty msg"))
				.map(v-> ofNullable(expression(v))
						.orElseThrow(()-> new IllegalArgumentException("illegal value : " + v)))
				.reduce(ComparisonExpression::or)
				.orElseThrow();
	}
	
	public static ComparisonExpressionBuilder<Object> ofComparator(DBComparator cmp) {
		return cmp::expression;
	}
}