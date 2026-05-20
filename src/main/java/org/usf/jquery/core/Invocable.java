package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Invocable extends QueryPart {
	
	@Override
	default int prepare(QueryAnalyzer composer) {
		throw new UnsupportedOperationException("compose operator");
	}
}
