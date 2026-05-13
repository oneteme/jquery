package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
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
	
	private final JoinType type;
	private final DBView view;
	private final Criteria[] criterias;
	
	ViewJoin(JoinType type, DBView view, Criteria[] criteria) {
		this.type = type;
		this.view = view;
		this.criterias = criteria;
	}
	
	@Override
	public int prepare(QueryManifest manifest) {
		if(type != CROSS) {
			requireAtLeastNArgs(1, criterias, ViewJoin.class::getSimpleName);
		}
		view.prepare(manifest);
		//aggregate after join, no need to declare groups
		return SCALAR;
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		var res = view.resolveView(query);
		query.append(type.name()).append(" JOIN ").append(res).appendSpace().appendViewAlias(res);
		if(!isEmpty(criterias)) {
			query.append(" ON ").appendEach(AND.sql(), criterias);
		} //else cross join
	}
	
	public ViewJoin criterias(Criteria... criterias) {
		if(isEmpty(criterias)) {
			return this;
		}
		if(isEmpty(this.criterias)) {
			return new ViewJoin(type, view, criterias);
		}
		return new ViewJoin(type, view, concat(stream(this.criterias), stream(criterias)).toArray(Criteria[]::new));
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	public static ViewJoin innerJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(INNER, view, criterias);
	}
	
	public static ViewJoin leftJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(LEFT, view, criterias);
	}
	
	public static ViewJoin rightJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(RIGHT, view, criterias);
	}

	public static ViewJoin fullJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(FULL, view, criterias);
	}

	public static ViewJoin crossJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(CROSS, view, criterias);
	}

	public static ViewJoin join(JoinType joinType, DBView view, Criteria... criterias) {
		return new ViewJoin(joinType, view, criterias);
	}
}
