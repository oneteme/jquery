package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public enum JQueryType implements JavaType {

	VIEW(DBView.class),
	COLUMN(TaggableColumn.class), //TD DBColumn !?
	FILTER(DBFilter.class),
	ORDER(DBOrder.class),
	QUERY(QueryView.class),
	PARTITION(Partition.class);
	
	private final Class<?> type;

	@Override
	public Class<?> typeClass() {
		return type;
	}

}
