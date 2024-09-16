package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

/**
 * 
 * @author u$f
 *
 */
public interface NamedColumn extends DBColumn {

	String getTag();
	
	default void sqlUsingTag(SqlStringBuilder sb, QueryContext ctx) {
		sql(sb, ctx);
		sb.runIfNonNull(getTag(), v-> sb.as(doubleQuote(v)));
	}
}