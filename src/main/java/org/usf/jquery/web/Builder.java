package org.usf.jquery.web;

import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import java.util.stream.Stream;

import org.usf.jquery.core.Chainable;
import org.usf.jquery.core.LogicalOperator;

/**
 * 
 * @author u$f
 *
 */
public interface Builder<T,V> {
	
	V build(T parent, Environment env, String... args);
	
	default V build(T parent, String... args) {
		return build(parent, currentEnvironment(), args);
	}
	
	static <T extends Chainable<T>> Builder<ViewDecorator, T> singleArgCriteria(ChainableCriteria<T> crt){
		return (view, env, args)-> crt.criteria(isEmpty(args) 
				? null 
				: requireNArgs(1, args, ()-> "single arg criteria")[0]);
	}

	static <T extends Chainable<T>> Builder<ViewDecorator, T> multiArgsCriteria(ChainableCriteria<T> crt){
		return multiArgsCriteria(OR, crt);
	}

	static <T extends Chainable<T>> Builder<ViewDecorator, T> multiArgsCriteria(LogicalOperator op, ChainableCriteria<T> crt){
		return (view, env, args)-> isEmpty(args) 
				? crt.criteria(null)
				: Stream.of(args).map(crt::criteria).reduce(op::combine).orElseThrow();
	}
}