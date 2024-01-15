package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public enum JqueryType implements JavaType {

	COLUMN(DBColumn.class), 
	ORDER(DBOrder.class), 
	CLAUSE(OperationColumn.class), 
	FILTER(DBFilter.class);
	//expression, WHEN_THEN, ...
	
	private final Class<?> type;

	@Override
	public Class<?> type() {
		return type;
	}

}
