package org.usf.jquery.web;

import org.usf.jquery.core.Chainable;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface ChainableCriteria<T extends Chainable<T>> {

	T criteria(String arg);
}
