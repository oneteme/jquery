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
	FILTER(DBFilter.class),
	ORDER(DBOrder.class),
	PARTITIONS(DBColumn[].class),
	COLUMNS(DBColumn[].class),
	FILTERS(DBFilter[].class),
	ORDERS(DBOrder[].class),
	QUERY(InternalQuery.class); //SELECT CLAUSE
	//expression, WHEN_THEN, ...
	
	private final Class<?> type;

	@Override
	public Class<?> typeClass() {
		return type;
	}

}
