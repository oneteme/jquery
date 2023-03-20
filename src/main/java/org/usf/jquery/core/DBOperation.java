package org.usf.jquery.core;

@FunctionalInterface
public interface DBOperation extends DBCallable {

	default boolean isAggregate() {
		return false;
	}
}
