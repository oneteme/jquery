package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireNoArgs;

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
	default int prepare(QueryManifest composer) {
		return -1; 
	}
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		build(query);
	}
	
	default DBView resolveView(QueryBuilder query) {
		var sub = query.subView(this);
		if(sub.isPresent()) {
			return sub.get().asReference();
		}
		else if(query.isCte(this)) {
			return asReference(); 
		}
		return this; //no mapping
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
	
	default ViewRef asReference() {
		return new ViewRef(this);
	}
	
	static DBView view(String name, String schema) {
		return b->{
			if(nonNull(schema)) {
				b.append(schema).append(".");
			}
			b.append(name);
		};
	}
	
	static DBView queryAsView(String sql) {
		return b-> b.appendParenthesis(()-> b.append(sql));
	}
	
	static DBView requestAsView(String req) {
		return null;
	}
}
