package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.ArrayList;

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
	public int compose(QueryDeclaration query) {
		return query.composeNested(expressions);
	}

	@Override
	public void build(QueryBuilder query, Object operand) {
		query.append("(").appendEach(operator.sql(), expressions, e-> e.build(query, operand)).append(")");
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
