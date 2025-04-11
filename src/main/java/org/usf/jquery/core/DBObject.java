package org.usf.jquery.core;

import static org.usf.jquery.core.QueryBuilder.addWithValue;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBObject {
	
	void build(QueryBuilder query, Object... args);
	
	static String toSQL(DBObject obj, Object... args) {
		var query = addWithValue();
		obj.build(query, args);
		return query.build().getSql();
	}
}
