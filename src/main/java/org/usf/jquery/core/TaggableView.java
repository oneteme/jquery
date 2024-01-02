package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableView extends DBView {

	String tagname();

	default String sql(QueryParameterBuilder builder, String schema, boolean as) {
		var s = this.sql(builder, schema);
		return as ? s + SPACE + builder.view(this) : s;
	}
}
