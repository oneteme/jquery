package org.usf.jquery.core;

import org.usf.jquery.web.proxy.PartitionComposer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public enum JQueryType implements JavaType {

	VIEW(DBView.class),
	QUERY(QueryView.class),
	COLUMN(Column.class),
	NAMED_COLUMN(NamedColumn.class),
	QUERY_COLUMN(SingleQueryColumn.class),
	FILTER(Criteria.class),
	ORDER(Order.class),
	JOIN(JoinComposer.class),
	PARTITION(PartitionComposer.class),
	CASE(CaseColumnComposer.class),
	UNION(QueryUnion.class);
	
	private final Class<?> type;
	
	@Override
	public Class<?> getCorrespondingClass() {
		return type;
	}
}
