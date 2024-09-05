package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

/**
 * 
 * @author u$f
 *
 */
public interface NamedColumn extends DBColumn {

	String getTag();
	
	default String sqlWithTag(QueryVariables builder) {
		var s = sql(builder);
		if(nonNull(getTag())) {
			s += " AS " + doubleQuote(getTag());
		}
		return s;
	}
}