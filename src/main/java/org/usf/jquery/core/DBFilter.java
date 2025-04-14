package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBObject, Chainable<DBFilter> {
	
	void build(QueryBuilder query);

	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, DBFilter.class::getSimpleName);
		build(query);
	}
}