package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBColumn.class::getSimpleName);
		return sql(builder);
	}

	default String sqlWithTag(QueryParameterBuilder builder) {
		return sql(builder) + SPACE + builder.view(this);
	}

	String sql(QueryParameterBuilder builder);

	default ViewQuery select(String tag, TaggableColumn... columns) {
		return new ViewQuery(tag, columns);
	}
	
	default ViewQuery window(String tag, TaggableColumn column) {
		return new ViewQuery(tag, allColumns(this).as(null), column);
	}
}
