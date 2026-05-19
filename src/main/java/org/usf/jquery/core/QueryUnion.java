package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class QueryUnion implements DBObject {
	
	private final boolean all;
	private final QueryView view;
	
	@Override
	public int prepare(QueryManifest manifest) {
		return view.prepare(manifest);
	}
	
	@Override
	public void build(QueryBuilder builder, Object... args) {
		requireNoArgs(args, QueryUnion.class::getSimpleName);
		builder.append(" UNION ");
		if(all) {
			builder.append("ALL ");
		}
		builder.append(view);
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
