package org.usf.jquery.core;

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
	
	@Override
	public String sql(QueryParameterBuilder builder, String schema) { //sub query should not use main builder
		var va = "v0";
		return new SqlStringBuilder(100)
		.append("(SELECT ").append(va).append(".*, ")
		.append(column.sql(addWithValue())).append(" AS ").append(doubleQuote(column.tagname()))
		.append(" FROM ").append(view.sql(addWithValue(), schema)).append(SPACE).append(va).append(")")
		.toString();
	}
	
	public DBFilter filter(ComparisonExpression expression) {
		DBColumn col = b-> member(b.overwriteView(this), column.tagname());
		return col.filter(expression);
	}

	@Override
	public String tagname() { //inherits tagname
		return view.tagname();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), "<schema>"); 
	}
	
}
