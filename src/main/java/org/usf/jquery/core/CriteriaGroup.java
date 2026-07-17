package org.usf.jquery.core;

import static java.util.Collections.unmodifiableCollection;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author u$f
 *
 */
//@see ComparisonExpressionGroup
public final class CriteriaGroup implements Criteria {
	
	private final LogicalOperator operator;
	private final Collection<Criteria> criterias;
	
	CriteriaGroup(LogicalOperator operator, Criteria... criterias) {
		if(isEmpty(criterias)) {
			throw new ComposeException("CriteriaGroup requires at least one filter");
		}
		this.operator = operator;
		this.criterias = chain(operator, criterias);
	}

	@Override
	public int prepare(QueryAnalyzer manifest) {
		return manifest.analyzeNested(criterias);
	}
	
	@Override
	public void build(SqlBuilder builder) {
		builder.append("(").appendEach(operator.sql(), criterias).append(")");
	}

	@Override
	public Criteria append(LogicalOperator op, Criteria filter) {
		return new CriteriaGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
	
	static Collection<Criteria> chain(LogicalOperator op, Criteria... filters) {
		var res = new ArrayList<Criteria>(filters.length);
		for(var f : filters) {
			if(f instanceof CriteriaGroup cg && cg.operator == op) {
				res.addAll(cg.criterias);
			}
			else {
				res.add(f);
			}
		}
		return unmodifiableCollection(res);
	}
}
