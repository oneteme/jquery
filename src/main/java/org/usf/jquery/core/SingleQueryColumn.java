package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public final class SingleQueryColumn implements QueryPart, Typed {
	
	private final Query view;
	private final JDBCType type;

	SingleQueryColumn(Query view) {
		if(isNull(view.getSelects()) || view.getSelects().size() != 1) {
			throw new IllegalArgumentException("SingleQueryColumn only accepts query with exactly one select column");
		}
		this.view = view;
		this.type = view.getSelects().iterator().next().getType();
	}

	@Override
	public void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, SingleQueryColumn.class::getSimpleName);
		view.build(builder);
	}
	
	@Override
	public int prepare(QueryAnalyzer manifest) {
		return view.prepare(manifest);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
