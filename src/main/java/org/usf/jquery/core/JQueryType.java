package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public enum JQueryType implements JavaType, TypeResolver {

	VIEW(DBView.class),
	QUERY(QueryView.class),
	COLUMN(Column.class),
	NAMED_COLUMN(NamedColumn.class),
	QUERY_COLUMN(SingleQueryColumn.class),
	FILTER(Criteria.class),
	ORDER(Order.class),
	JOIN(JoinsClause.class),
	PARTITION(Partition.class),
	CASE(CaseColumn.class),
	UNION(QueryUnion.class);
	
	private final Class<?> type;
	
	@Override
	public Class<?> getCorrespondingClass() {
		return type;
	}

	@Override
	public JavaType apply(Object[] t) {
		return this;
	}
}
