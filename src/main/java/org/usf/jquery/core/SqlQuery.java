package org.usf.jquery.core;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public final record SqlQuery (Store store,
		@NonNull String sql,TypedArg[] args){

	@Override
	public String toString() {
		return sql;
	}
}
