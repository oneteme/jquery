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
public final class Union implements QueryPart {
	
	private final boolean all;
	private final Query view;
	
	@Override
	public int prepare(QueryAnalyzer manifest) {
		return view.prepare(manifest);
	}
	
	@Override
	public void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, Union.class::getSimpleName);
		builder.append(" UNION ");
		if(all) {
			builder.append("ALL ");
		}
		builder.append(view);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
	
	public static Union union(Query view) {
		return new Union(false, view);
	}
	
	public static Union unionAll(Query view) {
		return new Union(true, view);
	}
}
