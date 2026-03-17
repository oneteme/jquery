package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.appendFirst;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SimplePredicate implements Predicate {

	private final Invocable comparator;
	private final Object[] right; //optional

	@Override
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		return DBObject.tryComposeNested(query, groupKeys, right);
	}
	
	@Override
	public void build(QueryBuilder query, Object left) {
		comparator.build(query, appendFirst(right, left));
	}
	
	@Override
	public Predicate append(LogicalOperator op, Predicate exp) {
		return new PredicateGroup(op, this, exp);
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this, Column.column("$item"));
	}
}
