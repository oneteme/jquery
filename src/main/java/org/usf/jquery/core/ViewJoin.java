package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.JoinType.CROSS;
import static org.usf.jquery.core.JoinType.FULL;
import static org.usf.jquery.core.JoinType.INNER;
import static org.usf.jquery.core.JoinType.LEFT;
import static org.usf.jquery.core.JoinType.RIGHT;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryContext.addWithValue;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.stream.Stream;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class ViewJoin implements DBObject {
	
	private final JoinType joinType;
	private final DBView view;
	private final DBFilter[] filters;
	//join results !?
	
	public ViewJoin(JoinType joinType, DBView view, DBFilter[] filters) {
		super();
		this.joinType = joinType;
		this.view = view;
		this.filters = joinType == CROSS 
				? filters 
				: requireAtLeastNArgs(1, filters, ViewJoin.class::getSimpleName);
	}

	@Override
	public String sql(QueryContext qv, Object[] args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		return sql(qv);
	}

	public String sql(QueryContext ctx) {
		var s = joinType + " JOIN " + view.sqlWithTag(ctx);
		if(!isEmpty(filters)) {
			var val = ctx.withValue();
			s += " ON " + Stream.of(filters)
			.map(f-> f.sql(val))
			.collect(joining(AND.sql()));
		}
		return s;
	}
	
	public static ViewJoin innerJoin(DBView view, DBFilter... filters) {
		return new ViewJoin(INNER, view, filters);
	}
	
	public static ViewJoin leftJoin(DBView view, DBFilter... filters) {
		return new ViewJoin(LEFT, view, filters);
	}
	
	public static ViewJoin rightJoin(DBView view, DBFilter... filters) {
		return new ViewJoin(RIGHT, view, filters);
	}

	public static ViewJoin fullJoin(DBView view, DBFilter... filters) {
		return new ViewJoin(FULL, view, filters);
	}

	public static ViewJoin crossJoin(DBView view, DBFilter... filters) {
		return new ViewJoin(CROSS, view, filters);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
