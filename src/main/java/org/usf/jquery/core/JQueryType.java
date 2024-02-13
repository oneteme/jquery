package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public enum JQueryType implements JavaType {

	COLUMN(TaggableColumn.class), //TD DBColumn !?
	FILTER(DBFilter.class),
	ORDER(DBOrder.class),
	QUERY(ViewQuery.class),
	PARTITION(Partition.class);
	//expression, WHEN_THEN, ...
	
	private final Class<?> type;

	@Override
	public Class<?> typeClass() {
		return type;
	}

}
