package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public final class SingleColumnQuery implements DBObject, Typed {
	
	private final QueryView view;
	private final JDBCType type;

	SingleColumnQuery(QueryView view) {
		var cols = view.getComposer().getColumns(); 
		if(cols.size() != 1) {
			throw new IllegalArgumentException("require only one column");
		}
		this.view = view;
		this.type = cols.get(0).getType();
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, SingleColumnQuery.class::getSimpleName);
		view.build(query);
	}
	
	@Override
	public int compose(QueryComposer composer, Consumer<DBColumn> groupKeys) {
		return view.compose(composer, groupKeys);
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
