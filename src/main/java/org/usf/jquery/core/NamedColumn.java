package org.usf.jquery.core;

import static java.util.Objects.nonNull;

/**
 * 
 * @author u$f
 *
 */
public interface NamedColumn extends DBColumn {

	String getTag();
	
	default void sqlUsingTag(SqlStringBuilder sb, QueryContext ctx) {
		sql(sb, ctx);
		var tag = getTag();
		if(nonNull(tag)) {
			sb.appendAs(tag);
		}
	}
}