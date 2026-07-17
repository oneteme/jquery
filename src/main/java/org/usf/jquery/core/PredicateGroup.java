package org.usf.jquery.core;

import static java.util.Collections.unmodifiableCollection;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
//@see ColumnFilterGroup
public final class PredicateGroup implements Predicate {
	
	private final LogicalOperator operator;
	private final Collection<Predicate> predicates;
	
	PredicateGroup(LogicalOperator operator, Predicate... predicates) {
		if(isEmpty(predicates)) {
			throw new ComposeException("PredicateGroup requires at least one predicate");
		}
		this.operator = operator;
		this.predicates = chain(operator, predicates);
	}
	
	@Override
	public int prepare(QueryAnalyzer manifest) {
		return manifest.analyzeNested(predicates);
	}

	@Override
	public void build(SqlBuilder builder, Object operand) {
		builder.append("(").appendEach(operator.sql(), predicates, e-> e.build(builder, operand)).append(")");
	}
	
	@Override
	public PredicateGroup append(LogicalOperator op, Predicate exp) {
		return new PredicateGroup(op, this, exp);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this, Column.column("$item"));
	}

	static Collection<Predicate> chain(LogicalOperator op, @NonNull Predicate... filters) {
		var res = new ArrayList<Predicate>(filters.length);
		for(var f : filters) {
			if(f instanceof PredicateGroup pg && pg.operator == op) {
				res.addAll(pg.predicates);
			}
			else {
				res.add(f);
			}
		}
		return unmodifiableCollection(res);
	}
}
