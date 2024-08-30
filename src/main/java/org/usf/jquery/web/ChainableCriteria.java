package org.usf.jquery.web;

import org.usf.jquery.core.Chainable;
import org.usf.jquery.core.QueryContext;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface ChainableCriteria<T extends Chainable<T>> {

	T criteria(QueryContext ctx, String arg);
}
