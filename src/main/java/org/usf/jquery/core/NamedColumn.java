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
	
	default String sqlWithTag(QueryContext ctx) {
		var s = sql(ctx);
		if(nonNull(getTag())) {
			s += " AS " + doubleQuote(getTag());
		}
		return s;
	}
}