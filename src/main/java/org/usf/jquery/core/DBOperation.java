package org.usf.jquery.core;

@FunctionalInterface
public interface DBOperation extends DBCallable {

	//do not change default value
	default boolean isAggregate() {
		return false;
	}
}
