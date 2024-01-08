package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableView extends DBView {

	String tagname();
	
	default String sqlWithTag(QueryParameterBuilder builder, String schema) {
		return sql(builder, schema) + SPACE + builder.view(this);
	}
}
