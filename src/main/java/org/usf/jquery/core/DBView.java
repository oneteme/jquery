package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {

	void sql(QueryBuilder query);
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		sql(query);
	}
	
	default void sqlUsingTag(QueryBuilder query) {
		sql(query);
		query.appendSpace().append(query.viewAlias(this));
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
