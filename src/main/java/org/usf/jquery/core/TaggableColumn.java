package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableColumn extends DBColumn {

	String tagname(); //JSON & TAG
	
	default String sql(QueryParameterBuilder builder, boolean as) {
		var s = this.sql(builder);
		return as ? s + " AS " + doubleQuote(tagname()) : s;
	}
}