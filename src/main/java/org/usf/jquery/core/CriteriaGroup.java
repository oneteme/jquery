package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.ArrayList;

/**
 * 
 * @author u$f
 *
 */
//@see ComparisonExpressionGroup
public final class CriteriaGroup implements Criteria {
	
	private final LogicalOperator operator;
	private final Criteria[] filters;
	
	CriteriaGroup(LogicalOperator operator, Criteria... filters) {
		this.operator = operator;
		this.filters = chain(operator, requireAtLeastNArgs(1, filters, CriteriaGroup.class::getSimpleName));
	}

	@Override
	public int compose(QueryDeclaration query) {
		return query.composeNested(filters);
	}
	
	@Override
	public void build(QueryBuilder query) {
		query.append("(").appendEach(operator.sql(), filters).append(")");
	}

	@Override
	public Criteria append(LogicalOperator op, Criteria filter) {
		return new CriteriaGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	static Criteria[] chain(LogicalOperator op, Criteria... filters) {
		var res = new ArrayList<Criteria>(filters.length);
		for(var f : filters) {
			if(f instanceof CriteriaGroup fg && fg.operator == op) {
				addAll(res, fg.filters);
			}
			else {
				res.add(f);
			}
		}
		return res.toArray(Criteria[]::new);
	}
}
