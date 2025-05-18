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
public interface Builder<T> {
	
	T build(ViewDecorator vd, QueryContext ctx, String... args);

	@Deprecated
	default T build(ViewDecorator vd, String... args) {
		return build(vd, null, args);
	}
	
	static <T extends Chainable<T>> Builder<T> singleArgCriteria(ChainableCriteria<T> cr){
		return (view, ctx, args)-> cr.criteria(isEmpty(args) ? null : requireNArgs(1, args, ()-> "single arg criteria")[0]);
	}

	static <T extends Chainable<T>> Builder<T> multiArgsCriteria(ChainableCriteria<T> cr){
		return multiArgsCriteria(OR, cr);
	}

	static <T extends Chainable<T>> Builder<T> multiArgsCriteria(LogicalOperator op, ChainableCriteria<T> cr){
		return (view, ctx, args)-> isEmpty(args) 
				? cr.criteria(null)
				: Stream.of(args).map(cr::criteria).reduce(op::combine).orElseThrow();
	}	
}
