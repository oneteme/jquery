package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class WindowView implements TaggableView {
	
	private final DBTable table;
	private final OverColumn column;
	private final String alias;
	private final ComparisonExpression expression;
	
	@Override
	public String sql(QueryParameterBuilder builder, String schema) {
		var tn = table.sql(builder, schema); //build tablename
		return new SqlStringBuilder(100)
		.append("(SELECT ").append(tn).append(".*, ")
		.append(column.sql(addWithValue())) //
		.append(" AS ").append(doubleQuote(alias))
		.append(" FROM ").append(tn).append(")")
		.toString();
	}

	public DBFilter filter() {
		return b-> expression.sql(b, column(b.columnFullReference(table.tagname(), doubleQuote(alias))));
	}

	@Override
	public String tagname() {
		return table.tagname();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), "<schema>"); 
	}
}
