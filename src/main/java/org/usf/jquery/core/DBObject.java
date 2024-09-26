package org.usf.jquery.core;

import static org.usf.jquery.core.QueryContext.addWithValue;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBObject {
	
	void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args);
	
	static String toSQL(DBObject obj, Object... args) {
		var sb = new SqlStringBuilder();
		obj.sql(sb, addWithValue(), args);
		return sb.toString();
	}
}
