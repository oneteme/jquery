package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public enum JQueryType implements JavaType, TypeResolver {

	VIEW(View.class),
	QUERY(Query.class),
	COLUMN(Column.class),
//	NAMED_COLUMN(NamedColumn.class),
	QUERY_COLUMN(SingleQueryColumn.class),
	FILTER(Criteria.class),
	ORDER(Order.class),
	JOIN(JoinGroup.class),
	PARTITION(Partition.class),
	CASE(CaseColumn.class),
	UNION(Union.class),
	GROUP(Group.class);
	
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
