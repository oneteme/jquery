package org.usf.jquery.core;

import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Invocable extends DBObject {
	
	@Override
	default int compose(QueryComposer composer, Consumer<Column> groupKeys) {
		throw new UnsupportedOperationException("compose operator");
	}
}
