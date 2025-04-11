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

import java.util.Objects;
import java.util.function.Consumer;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class ViewJoin implements DBObject, Nested {
	
	private final JoinType joinType;
	private final DBView view;
	private final DBFilter[] filters;
	//join results !?
	
	ViewJoin(JoinType joinType, DBView view, DBFilter[] filters) {
		this.joinType = joinType;
		this.view = view;
		this.filters = joinType == CROSS 
				? filters 
				: requireAtLeastNArgs(1, filters, ViewJoin.class::getSimpleName);
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		query.append(joinType.name()).append(" JOIN ").append(view).appendAs().appendViewAlias(view);
		if(!isEmpty(filters)) {
			query.append(" ON ").append(AND.sql(), filters);
		} //else cross join
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		query.declare(view);
		return Nested.aggregation(query, groupKeys, filters);
	}
	
	public ViewJoin map(DBView view) {
		return Objects.equals(this.view, view) 
				? this 
				: new ViewJoin(joinType, view, filters); 
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
