package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Chainable<T extends Chainable<T>> {
	
	T append(LogicalOperator op, T exp);
	
	default T and(T exp) {
		return append(AND, exp);
	}

	default T or(T exp) {
		return append(OR, exp);
	}
}
