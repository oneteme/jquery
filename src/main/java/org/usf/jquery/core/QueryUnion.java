package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryUnion implements DBObject, Nested {
	
	private final boolean all;
	private final QueryView view;
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, QueryUnion.class::getSimpleName);
		query.append(" UNION ");
		if(all) {
			query.append("ALL ");
		}
		query.append(view);
	}
	
	@Override
	public int compose(QueryComposer composer, Consumer<DBColumn> groupKeys) {
		return view.compose(composer, groupKeys);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	public static QueryUnion union(QueryView view) {
		return new QueryUnion(false, view);
	}
	
	public static QueryUnion unionAll(QueryView view) {
		return new QueryUnion(true, view);
	}
}
