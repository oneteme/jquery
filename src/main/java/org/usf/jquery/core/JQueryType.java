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
	COLUMN(DBColumn.class),
	NAMED_COLUMN(NamedColumn.class),
	QUERY_COLUMN(SingleColumnQuery.class),
	FILTER(DBFilter.class),
	ORDER(DBOrder.class),
	QUERY(QueryView.class),
	JOIN(ViewJoin[].class),
	PARTITION(Partition.class);
	
	private final Class<?> type;
	
	@Override
	public Class<?> getCorrespondingClass() {
		return type;
	}
}
