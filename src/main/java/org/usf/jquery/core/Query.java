package org.usf.jquery.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public final class Query {
	
	@NonNull
	private final String sql;
	private final TypedArg[] args;
	
	@Override
	public String toString() {
		return sql;
	}
}
