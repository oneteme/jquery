package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.SqlStringBuilder.member;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class WindowView implements TaggableView {
	
	private final TaggableView view;
	private final TaggableColumn column; //named operation column
	private final ComparisonExpression expression;
	
	@Override
	public String sql(QueryParameterBuilder builder, String schema) {
		var va = "v0";
		return new SqlStringBuilder(100)
		.append("(SELECT ").append(va).append(".*, ")
		.append(column.sql(addWithValue())).append(" AS ").append(doubleQuote(column.tagname()))
		.append(" FROM ").append(view.sql(builder, schema)).append(SPACE).append(va).append(")")
		.toString();
	}

	public DBFilter filter() {
		return b-> expression.sql(b, column(member(b.view(view), doubleQuote(column.tagname()))));
	}

	@Override
	public String tagname() {
		return view.tagname(); //inherits tagname
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), "<schema>"); 
	}
}
