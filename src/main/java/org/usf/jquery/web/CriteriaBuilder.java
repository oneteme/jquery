package org.usf.jquery.web;

import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;

import java.util.stream.Stream;

import org.usf.jquery.core.Chainable;
import org.usf.jquery.core.LogicalOperator;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface CriteriaBuilder<T extends Chainable<T>> {
	
	T criteria(String arg);
	
	default T build(String... args) {
		return Stream.of(requireAtLeastNArgs(1, args, CriteriaBuilder.class::getSimpleName))
				.map(v-> ofNullable(criteria(v))
						.orElseThrow(()-> cannotEvaluateException("criteria value", v)))
				.reduce((e1, e2)-> e1.append(combiner(), e2))
				.orElseThrow();
	}
	
	default LogicalOperator combiner() {
		return OR;
	}
}
