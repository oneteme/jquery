package org.usf.jquery.core;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.appendFirst;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SimplePredicate implements Predicate {

	private final Invocable invokable;
	private final Object[] right; //optional

	@Override
	public int prepare(QueryManifest declare) {
		return nonNull(right) ? declare.tryPrepareNested(asList(right)) : SCALAR;
	}
	
	@Override
	public void build(QueryBuilder builder, Object left) {
		invokable.build(builder, appendFirst(right, left));
	}
	
	@Override
	public Predicate append(LogicalOperator op, Predicate predicate) {
		return new PredicateGroup(op, this, predicate);
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this, Column.column("$item"));
	}
}
