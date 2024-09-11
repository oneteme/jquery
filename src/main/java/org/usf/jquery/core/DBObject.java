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
	
	static String toSQL(DBObject obj, Object... values) {
		var sb = new SqlStringBuilder();
		obj.sql(sb, addWithValue(), values);
		return sb.toString();
	}
}
