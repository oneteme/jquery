package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public interface Chainable<T extends Chainable<T>> {
	
	T append(LogicalOperator op, T exp);
	
	default T and(T exp) {
		return append(AND, exp);
	}

	default T or(T exp) {
		return append(OR, exp);
	}
	

	static <T, C extends Chainable<C>> C or(T[] values, Function<T, C> builder) {
		return chain(OR, values, builder);
	}
	
	static <T, C extends Chainable<C>> C  and(T[] values, Function<T, C> builder) {
		return chain(AND, values, builder);
	}
	
	static <T, C extends Chainable<C>> C chain(LogicalOperator op, T[] values, Function<T, C> builder) {
		return Stream.of(values).map(builder).reduce(op::combine).orElseThrow();
	}
}
