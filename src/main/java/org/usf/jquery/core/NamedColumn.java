package org.usf.jquery.core;

import static java.util.Objects.nonNull;

/**
 * 
 * @author u$f
 *
 */
public interface NamedColumn extends DBColumn {

	String getTag();
	
	default void sqlUsingTag(QueryBuilder query) {
		build(query);
		var tag = getTag();
		if(nonNull(tag)) {
			query.appendAs().append(tag);
		}
	}
}