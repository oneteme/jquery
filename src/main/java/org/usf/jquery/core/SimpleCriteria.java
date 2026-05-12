package org.usf.jquery.core;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class SimpleCriteria implements Criteria {

	private final Object left;
	private final Predicate expression;

	@Override
	public int prepare(QueryManifest query) {
		var v = expression.prepare(query);
		if(left instanceof DBObject c) {
			return Math.max(v, c.prepare(query));
		}
		return v;
	}
	
	@Override
	public void build(QueryBuilder query) {
		expression.build(query, left);
	}

	@Override
	public CriteriaGroup append(LogicalOperator op, Criteria filter) {
		return new CriteriaGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
