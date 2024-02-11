package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

import java.util.Objects;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableColumn extends DBColumn {

	String tagname(); //JSON & TAG
	
	default String sqlWithTag(QueryParameterBuilder builder) {
		var s = sql(builder);
		return Objects.isNull(tagname()) ? s : s  + " AS " + doubleQuote(tagname());
	}
}