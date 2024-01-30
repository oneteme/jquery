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
	CLAUSE(OperationColumn.class), //SELECT, WHERE, ORDER, PARTITION
	FILTER(DBFilter.class),
	QUERY(RequestQueryBuilder.class); //SELECT CLAUSE
	//expression, WHEN_THEN, ...
	
	private final Class<?> type;

	@Override
	public Class<?> typeClass() {
		return type;
	}

}
