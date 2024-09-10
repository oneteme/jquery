package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.Nested.viewsOfNested;
import static org.usf.jquery.core.QueryContext.addWithValue;
import static org.usf.jquery.core.Utils.appendLast;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Collection;
import java.util.stream.Stream;

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
	public String sql(QueryContext ctx, Object operand) {
		return Stream.of(expressions)
		.map(o-> o.sql(ctx, operand))
		.collect(joining(operator.sql(), "(", ")"));
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
		return sql(addWithValue(), "<left>");
	}
}
