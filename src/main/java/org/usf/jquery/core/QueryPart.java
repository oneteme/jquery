package org.usf.jquery.core;

import static org.usf.jquery.core.SqlBuilder.addWithValue;
import static org.usf.jquery.core.Stores.NO_STORE;

/**
 * 
 * @author u$f
 *
 */
public interface QueryPart {

	static final int SCALAR = -1;
	static final int DIMENSION = 0;
	static final int MEASURE = 1;

	int prepare(QueryAnalyzer analyzer);
	
	void build(SqlBuilder builder, Object... args);
	
	static String toSQL(QueryPart obj, Object... args) {
		var query = addWithValue(NO_STORE);
		obj.build(query, args);
		return query.build().sql();
	}
}
