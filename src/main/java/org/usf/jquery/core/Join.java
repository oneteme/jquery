package org.usf.jquery.core;

import static org.usf.jquery.core.JoinType.CROSS;
import static org.usf.jquery.core.JoinType.FULL;
import static org.usf.jquery.core.JoinType.INNER;
import static org.usf.jquery.core.JoinType.LEFT;
import static org.usf.jquery.core.JoinType.RIGHT;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryAnalyzer.IGNORE_GROUPS;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toList;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class Join implements QueryPart {
	
	private final JoinType type;
	private final View view;
	private final Collection<Criteria> criterias;

	Join(JoinType type, View view, Collection<Criteria> criterias) {
		if(type != CROSS && isEmpty(criterias)) {
			throw new ComposeException("Join type " + type + " requires at least one criteria");
		}
		this.type = type;
		this.view = view;
		this.criterias = criterias;
	}
	
	@Override
	public int prepare(QueryAnalyzer analyzer) {
		view.prepare(analyzer);
		if(isEmpty(criterias) || analyzer.with(IGNORE_GROUPS).analyzeNested(criterias) == DIMENSION) {
			return SCALAR;
		}
		throw new IllegalStateException("Join criteria must be a scalar expression");
	}

	@Override
	public void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, Join.class::getSimpleName);
		builder.append(type.name()).append(" JOIN");
		if(!builder.isCte(view)) {
			builder.appendSpace().append(view);
		}
		builder.appendSpace().appendViewAlias(view);
		if(!isEmpty(criterias)) {
			builder.append(" ON ").appendEach(AND.sql(), criterias);
		}
	}
		
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
	
	public static Join innerJoin(View view, Criteria... criterias) {
		return new Join(INNER, view, toList(criterias));
	}
	
	public static Join leftJoin(View view, Criteria... criterias) {
		return new Join(LEFT, view, toList(criterias));
	}
	
	public static Join rightJoin(View view, Criteria... criterias) {
		return new Join(RIGHT, view, toList(criterias));
	}

	public static Join fullJoin(View view, Criteria... criterias) {
		return new Join(FULL, view, toList(criterias));
	}

	public static Join crossJoin(View view, Criteria... criterias) {
		return new Join(CROSS, view, toList(criterias));
	}

	public static Join join(JoinType joinType, View view, Criteria... criterias) {
		return new Join(joinType, view, toList(criterias));
	}
}
