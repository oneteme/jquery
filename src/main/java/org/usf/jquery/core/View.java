package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface View extends QueryPart {

	void build(SqlBuilder builder);
	
	@Override
	default int prepare(QueryAnalyzer analyzer) {
		return SCALAR; //do not declare self on analyzer
	}
	
	@Override
	default void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, View.class::getSimpleName);
		build(builder);
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
