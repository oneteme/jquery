package org.usf.jquery.web;

import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;

import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.Comparator;
import org.usf.jquery.core.LogicalOperator;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface CriteriaBuilder<T> {
	
	ComparisonExpression criteria(T arg);
	
	default LogicalOperator combiner() {
		return OR;
	}
	
	@SuppressWarnings("unchecked")
	default ComparisonExpression build(T... args) {
		return Stream.of(requireAtLeastNArgs(1, args, CriteriaBuilder.class::getSimpleName))
				.map(v-> ofNullable(criteria(v))
						.orElseThrow(()-> cannotEvaluateException("criteria value", v.toString())))
				.reduce(ComparisonExpression::or)
				.orElseThrow();
	}
	
	public static CriteriaBuilder<Object> ofComparator(Comparator cmp) {
		return cmp::expression;
	}
}
