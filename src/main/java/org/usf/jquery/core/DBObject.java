package org.usf.jquery.core;

import static org.usf.jquery.core.QueryBuilder.addWithValue;

/**
 * 
 * @author u$f
 *
 */
public interface DBObject {

	//0: groupKey, +1: aggregation, -1: constant
	int compose(QueryDeclaration composer);
	
	void build(QueryBuilder query, Object... args);
	
	static String toSQL(DBObject obj, Object... args) {
		var query = addWithValue();
		obj.build(query, args);
		return query.build().sql();
	}	
}
