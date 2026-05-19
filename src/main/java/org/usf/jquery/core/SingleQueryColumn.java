package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public final class SingleQueryColumn implements DBObject, Typed {
	
	private final QueryView view;
	private final JDBCType type;

	SingleQueryColumn(QueryView view) {
		var cols = requireNArgs(1, view.getSelects(), SingleQueryColumn.class::getSimpleName);
		this.view = view;
		this.type = cols[0].getType();
	}

	@Override
	public void build(QueryBuilder builder, Object... args) {
		requireNoArgs(args, SingleQueryColumn.class::getSimpleName);
		view.build(builder);
	}
	
	@Override
	public int prepare(QueryManifest manifest) {
		return view.prepare(manifest);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
