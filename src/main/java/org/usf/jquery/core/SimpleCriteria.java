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
	public int prepare(QueryManifest manifest) {
		var v = predicate.prepare(manifest);
		return left instanceof DBObject c ? Math.max(v, c.prepare(manifest)) : v;
	}
	
	@Override
	public void build(QueryBuilder builder) {
		predicate.build(builder, left);
	}

	@Override
	public CriteriaGroup append(LogicalOperator op, Criteria criteria) {
		return new CriteriaGroup(op, this, criteria);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
