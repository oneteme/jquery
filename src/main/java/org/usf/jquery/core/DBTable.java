package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBTable extends DBObject {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBTable.class::getSimpleName);
		var sql = sql(builder);
		return sql.matches("^\\w+$") ? sql : parenthese(sql); //table or query
	}

	String sql(QueryParameterBuilder builder);
	
	default NamedTable as(String name) { // map
		return new NamedTable(this, name);
	}
	
}
