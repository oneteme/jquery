package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.Validation.requireNoArgs;
import static org.usf.jquery.core.ViewJoin.JoinType.FULL;
import static org.usf.jquery.core.ViewJoin.JoinType.INNER;
import static org.usf.jquery.core.ViewJoin.JoinType.LEFT;
import static org.usf.jquery.core.ViewJoin.JoinType.RIGHT;

import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public class ViewJoin implements DBObject {
	
	private final JoinType joinType;
	private final DBView view;
	private final DBFilter[] filters;
	//join results !?

	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		return sql(builder);
	}

	public String sql(QueryParameterBuilder builder) {
		return joinType + " JOIN " + view.sqlWithTag(builder) + " ON " +
				Stream.of(filters).map(f-> f.sql(builder)).collect(joining(AND.sql()));
	}
	
	public static ViewJoin innerJoin(DBView view, DBFilter... filters) {
		return join(INNER, view, filters);
	}
	
	public static ViewJoin leftJoin(DBView view, DBFilter... filters) {
		return join(LEFT, view, filters);
	}
	
	public static ViewJoin rightJoin(DBView view, DBFilter... filters) {
		return join(RIGHT, view, filters);
	}

	public static ViewJoin fullJoin(DBView view, DBFilter... filters) {
		return join(FULL, view, filters);
	}
	
	public static ViewJoin join(JoinType jt, DBView view, DBFilter... filters) {
		return new ViewJoin(jt, view, filters);
	}
	
	public enum JoinType {

		INNER, LEFT, RIGHT, FULL;
	}
}
