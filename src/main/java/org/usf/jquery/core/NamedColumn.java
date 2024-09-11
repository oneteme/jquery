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
	
	default void sqlWithTag(SqlStringBuilder sb, QueryContext ctx) {
		sql(sb, ctx);
		sb.appendIf(nonNull(getTag()), ()-> " AS " + doubleQuote(getTag()));
	}
}