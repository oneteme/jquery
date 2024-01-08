package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableColumn extends DBColumn {

	String tagname(); //JSON & TAG
	
	default String sqlWithTag(QueryParameterBuilder builder) {
		return sql(builder) + " AS " + doubleQuote(tagname());
	}
}