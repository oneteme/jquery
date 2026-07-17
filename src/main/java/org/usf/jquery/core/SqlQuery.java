package org.usf.jquery.core;

import java.util.List;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public final record SqlQuery (@NonNull String sql, List<TypedArg> args){

	@Override
	public String toString() {
		return sql;
	}
}
