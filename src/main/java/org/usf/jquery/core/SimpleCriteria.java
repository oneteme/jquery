package org.usf.jquery.core;

import java.util.function.Consumer;

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
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		return DBObject.tryComposeNested(query, groupKeys, left, expression);
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
