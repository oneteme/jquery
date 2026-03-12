package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Comparator extends invocable {

	default SimplePredicate expression(Object... right) {
		return new SimplePredicate(this, right, null);
	}
	
	@Deprecated
	default SimplePredicate expression(Adjuster<Object[]> adj, Object... initalValue) {
		return new SimplePredicate(this, initalValue, adj);
	}
}
	
