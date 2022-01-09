package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ColumnFilter implements DBFilter {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final OperatorExpression expression;

	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		return expression.sql(column.sql(table, ph), ph);
	}

	@Override
	public ColumnFilterGroup append(LogicalOperator op, DBFilter filter) {
		return new ColumnFilterGroup(op, this, filter);
	}
	
	public DBFilter and(OperatorExpression exp) {
		return append(AND, exp);
	}
	
	public DBFilter or(OperatorExpression exp) {
		return append(OR, exp);
	}

	public ColumnFilter append(LogicalOperator op, OperatorExpression exp) {
		var nex = exp.append(op, exp); //@see OperatorExpressionGroup
		return nex == exp ? this : new ColumnFilter(column, nex);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
}
