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

import java.util.function.Consumer;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class ViewJoin implements DBObject {
	
	private final JoinType type;
	private final DBView view;
	private final DBFilter[] filters;
	
	ViewJoin(JoinType type, DBView view, DBFilter[] filters) {
		this.type = type;
		this.view = view;
		this.filters = type == CROSS 
				? filters 
				: requireAtLeastNArgs(1, filters, ViewJoin.class::getSimpleName);
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		query.declare(view); //if filters is null
		return DBObject.composeNested(query, groupKeys, filters);
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		var res = view.resolveView(query);
		query.append(type.name()).append(" JOIN ").append(res).appendSpace().appendViewAlias(res);
		if(!isEmpty(filters)) {
			query.append(" ON ").appendEach(AND.sql(), filters);
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

	public static ViewJoin join(JoinType joinType, DBView view, DBFilter... filters) {
		return new ViewJoin(joinType, view, filters);
	}
}
