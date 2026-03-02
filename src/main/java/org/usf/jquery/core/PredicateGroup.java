package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
//@see ColumnFilterGroup
public final class PredicateGroup implements Predicate {
	
	private final LogicalOperator operator;
	private final Predicate[] expressions;
	
	PredicateGroup(LogicalOperator operator, Predicate... expressions) {
		this.operator = operator;
		this.expressions = chain(operator, requireAtLeastNArgs(1, expressions, PredicateGroup.class::getSimpleName));
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		return DBObject.composeNested(query, groupKeys, expressions);
	}

	@Override
	public void build(QueryBuilder query, Object operand) {
		query.appendParenthesis(()-> query.appendEach(operator.sql(), expressions, e-> e.build(query, operand)));
	}
	
	@Override
	public Predicate append(LogicalOperator op, Predicate exp) {
		return new PredicateGroup(op, this, exp);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this, "<left>");
	}

	static Predicate[] chain(LogicalOperator op, Predicate... filters) {
		var res = new ArrayList<Predicate>(filters.length);
		for(var f : filters) {
			if(f instanceof PredicateGroup fg && fg.operator == op) {
				addAll(res, fg.expressions);
			}
			else {
				res.add(f);
			}
		}
		return res.toArray(Predicate[]::new);
	}
}
