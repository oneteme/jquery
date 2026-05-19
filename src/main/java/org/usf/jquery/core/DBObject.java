package org.usf.jquery.core;

import static org.usf.jquery.core.QueryBuilder.addWithValue;

/**
 * 
 * @author u$f
 *
 */
public interface DBObject {

	static final int SCALAR = -1;
	static final int DIMENSION = 0;
	static final int MEASURE = 1;

	int prepare(QueryManifest manifest);
	
	void build(QueryBuilder builder, Object... args);
	
	static String toSQL(DBObject obj, Object... args) {
		var query = addWithValue();
		obj.build(query, null, args);
		return query.build().sql();
	}
}
