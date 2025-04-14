package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {

	void build(QueryBuilder query);
	
	/**
	 * do not declare self on composer
	 */
	@Override
	default int compose(QueryComposer composer, Consumer<DBColumn> groupKeys) {
		return -1; 
	}
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		build(query);
	}
	
	default ViewColumn column(String name) {
		return new ViewColumn(name, this, null, null);
	}

	default ViewColumn column(String name, JDBCType type) {
		return new ViewColumn(name, this, type, null);
	}
	
	default ViewColumn column(String name, JDBCType type, String tag) {
		return new ViewColumn(name, this, type, tag);
	}
}
