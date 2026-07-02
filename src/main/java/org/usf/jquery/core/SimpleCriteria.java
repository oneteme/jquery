package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class SimpleCriteria implements Criteria {

	private final Object left;
	private final Predicate predicate;

	@Override
	public int prepare(QueryAnalyzer manifest) {
		var v = predicate.prepare(manifest);
		return left instanceof QueryPart p ? Math.max(v, p.prepare(manifest)) : v;
	}
	
	@Override
	public void build(SqlBuilder builder) {
		predicate.build(builder, left);
	}

	@Override
	public CriteriaGroup append(LogicalOperator op, Criteria criteria) {
		return new CriteriaGroup(op, this, criteria);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
