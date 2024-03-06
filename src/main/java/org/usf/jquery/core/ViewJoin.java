package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ViewJoin implements DBObject  {
	
	private final DBView view;
	private final JoinType joinType;
	private final Collection<DBFilter> filters;

	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBColumn.class::getSimpleName);
		return sql(builder);
	}

	public String sql(QueryParameterBuilder builder) {
		return joinType + " JOIN " + view.sqlWithTag(builder) + 
				" ON " + filters.stream().map(f-> f.sql(builder)).collect(joining(AND.sql()));
	}
	
	public String id() {
		return view.id();
	}
	
	enum JoinType {

		INNER, LEFT, RIGHT, FULL {
			@Override
			public String toString() {
				return name() + " OUTER";
			}
		};
	}

}
