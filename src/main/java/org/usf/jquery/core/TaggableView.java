package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableView extends DBView {

	String tagname();
	
	default String sqlWithTag(QueryParameterBuilder builder) {
		return sql(builder) + SPACE + builder.view(this);
	}
}
