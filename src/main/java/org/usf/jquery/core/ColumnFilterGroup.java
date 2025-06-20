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
//@see ComparisonExpressionGroup
public final class ColumnFilterGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final DBFilter[] filters;
	
	ColumnFilterGroup(LogicalOperator operator, DBFilter... filters) {
		this.operator = operator;
		this.filters = chain(operator, requireAtLeastNArgs(1, filters, ColumnFilterGroup.class::getSimpleName));
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return DBObject.composeNested(query, groupKeys, filters);
	}
	
	@Override
	public void build(QueryBuilder query) {
		query.appendParenthesis(()-> query.appendEach(operator.sql(), filters));
	}

	@Override
	public DBFilter append(LogicalOperator op, DBFilter filter) {
		return new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	static DBFilter[] chain(LogicalOperator op, DBFilter... filters) {
		var res = new ArrayList<DBFilter>(filters.length);
		for(var f : filters) {
			if(f instanceof ColumnFilterGroup fg && fg.operator == op) {
				addAll(res, fg.filters);
			}
			else {
				res.add(f);
			}
		}
		return res.toArray(DBFilter[]::new);
	}
}
