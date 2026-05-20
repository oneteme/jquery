package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.appendFirst;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toList;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
public final class SimplePredicate implements Predicate {

	private final Invocable invokable;
	private final Object[] right; //optional
	private final int from;
	
	public SimplePredicate(Invocable invokable, Object[] right) {
		this(invokable, right, 0);
	}
	
	public SimplePredicate(Invocable invokable, Object[] right, int from) {
		var size = nonNull(right) ? right.length : 0;
		if(from < 0 || from > size) {
			throw new IllegalArgumentException("from index must be between 0 and " + size);
		}
		this.invokable = invokable;
		this.right = right;
		this.from = from;
	}

	@Override
	public int prepare(QueryAnalyzer declare) {
		return nonNull(right) ? declare.tryAnalyzeNested(toList(right)) : SCALAR;
	}
	
	@Override
	public void build(SqlBuilder builder, Object left) {
		Object[] args = null;
		if(isEmpty(right)) {
			args = new Object[] {left};
		}
		else if(from == 0) {
			args = appendFirst(right, left);
		}
		else if(from == 1 && right[0] == left) {
			args = right; //left is already the first argument, no need to append
		}
		else {
			throw new IllegalStateException("invalid arguments configuration for " + this);
		}
		invokable.build(builder, args);
	}
	
	@Override
	public Predicate append(LogicalOperator op, Predicate predicate) {
		return new PredicateGroup(op, this, predicate);
	}

	@Override
	public String toString() {
		return QueryPart.toSQL(this, Column.column("$item"));
	}
}
