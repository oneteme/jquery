package org.usf.jquery.core;

import static org.usf.jquery.core.JoinType.CROSS;
import static org.usf.jquery.core.JoinType.FULL;
import static org.usf.jquery.core.JoinType.INNER;
import static org.usf.jquery.core.JoinType.LEFT;
import static org.usf.jquery.core.JoinType.RIGHT;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;
import static org.usf.jquery.core.Validation.requireNoArgs;

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
		this.joinType = joinType;
		this.view = view;
		this.filters = joinType == CROSS 
				? filters 
				: requireAtLeastNArgs(1, filters, ViewJoin.class::getSimpleName);
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		sql(sb, ctx);
	}

	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.append(joinType.name()).append(" JOIN ");
		ctx.appendView(sb, view);
		if(!isEmpty(filters)) {
			sb.append(" ON ").runForeach(filters, AND.sql(), f-> f.sql(sb, ctx));
		} //else cross join
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
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
}
