package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Invocable extends DBObject {
	
	@Override
	default int prepare(QueryManifest composer) {
		throw new UnsupportedOperationException("compose operator");
	}
}
