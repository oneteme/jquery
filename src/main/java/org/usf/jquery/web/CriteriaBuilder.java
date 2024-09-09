package org.usf.jquery.web;

import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;

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
	
	T build(String... arg);

	static <T extends Chainable<T>> CriteriaBuilder<T> singleArg(ChainableCriteria<T> cr){
		return args-> cr.criteria(isEmpty(args) ? null : requireNArgs(1, args, ()-> "single arg criteria")[0]);
	}

	static <T extends Chainable<T>> CriteriaBuilder<T> multiArgs(ChainableCriteria<T> cr){
		return multiArgs(OR, cr);
	}

	static <T extends Chainable<T>> CriteriaBuilder<T> multiArgs(LogicalOperator op, ChainableCriteria<T> cr){
		return args-> isEmpty(args) 
				? cr.criteria(null)
				: Stream.of(args).map(cr::criteria).reduce(op::combine).orElseThrow();
	}
}
