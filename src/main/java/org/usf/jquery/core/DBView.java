package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {

	String sql(QueryParameterBuilder builder);
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		return sql(builder);
	}
	
	default String sqlWithTag(QueryParameterBuilder builder) {
		var tag = builder.view(this);
		var sql = sql(builder);
		return isNull(tag) ? sql : sql + SPACE + tag;
	}
}
