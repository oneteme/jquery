package org.usf.jquery.core;

import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.Nested.viewsOfNested;
import static org.usf.jquery.core.Utils.appendLast;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Collection;

/**
 * 
 * @author u$f
 *
 */
//@see ColumnFilterGroup
public final class ComparisonExpressionGroup implements ComparisonExpression {
	
	private final LogicalOperator operator;
	private final ComparisonExpression[] expressions;
	
	ComparisonExpressionGroup(LogicalOperator operator, ComparisonExpression... expressions) {
		this.operator = operator;
		this.expressions = requireAtLeastNArgs(1, expressions, ComparisonExpressionGroup.class::getSimpleName);
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object operand) {
		sb.parenthesis(()-> 
			sb.runForeach(expressions, operator.sql(), o-> o.sql(sb, ctx, operand)));
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) {
		return resolveAll(expressions, builder);
	}
	
	@Override
	public void views(Collection<DBView> views) {
		viewsOfNested(views, expressions);
	}
	
	@Override
	public ComparisonExpression append(LogicalOperator op, ComparisonExpression exp) {
		return operator == op 
				? new ComparisonExpressionGroup(op, appendLast(expressions, exp))
		        : new ComparisonExpressionGroup(op, this, exp);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this, "<left>");
	}
}
