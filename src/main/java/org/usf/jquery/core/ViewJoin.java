package org.usf.jquery.core;

import static java.util.Arrays.asList;
import static org.usf.jquery.core.JoinType.CROSS;
import static org.usf.jquery.core.JoinType.FULL;
import static org.usf.jquery.core.JoinType.INNER;
import static org.usf.jquery.core.JoinType.LEFT;
import static org.usf.jquery.core.JoinType.RIGHT;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

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
	private final Collection<Criteria> criterias;

	ViewJoin(JoinType type, DBView view, Collection<Criteria> criterias) {
		if(type != CROSS && isEmpty(criterias)) {
			throw new ComposeException("Join type " + type + " requires at least one criteria");
		}
		this.type = type;
		this.view = view;
		this.criterias = criterias;
	}
	
	@Override
	public int prepare(QueryManifest manifest) {
		view.prepare(manifest);
		return SCALAR;
	}

	@Override
	public void build(QueryBuilder builder, Object... args) {
		requireNoArgs(args, ViewJoin.class::getSimpleName);
		builder.append(type.name()).append(" JOIN ");
		if(!builder.isCte(view)) {
			builder.append(view).appendSpace();
		}
		builder.appendViewAlias(view);
		if(!isEmpty(criterias)) {
			builder.append(" ON ").appendEach(AND.sql(), criterias);
		}
	}
		
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	public static ViewJoin innerJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(INNER, view, asList(criterias));
	}
	
	public static ViewJoin leftJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(LEFT, view, asList(criterias));
	}
	
	public static ViewJoin rightJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(RIGHT, view, asList(criterias));
	}

	public static ViewJoin fullJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(FULL, view, asList(criterias));
	}

	public static ViewJoin crossJoin(DBView view, Criteria... criterias) {
		return new ViewJoin(CROSS, view, asList(criterias));
	}

	public static ViewJoin join(JoinType joinType, DBView view, Criteria... criterias) {
		return new ViewJoin(joinType, view, asList(criterias));
	}
}
