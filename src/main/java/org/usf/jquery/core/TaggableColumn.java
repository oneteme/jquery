package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

/**
 * 
 * @author u$f
 *
 */
public interface TaggableColumn extends DBColumn {

	String tagname(); //JSON & TAG
	
	default String sqlWithTag(QueryParameterBuilder builder) {
		var s = sql(builder);
		if(nonNull(tagname())) {
			s += " AS " + doubleQuote(tagname());
		}
		return s;
	}
}