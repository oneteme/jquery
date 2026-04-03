package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Invocable extends DBObject {
	
	@Override
	default int compose(QueryDeclaration composer) {
		throw new UnsupportedOperationException("compose operator");
	}
}
