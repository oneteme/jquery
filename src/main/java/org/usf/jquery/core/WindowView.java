package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.SqlStringBuilder.member;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowView implements Query {
	
	private final TaggableView view;
	private final TaggableColumn column; //named operation column
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		var b = builder.subQuery(); //sub query should not use main builder
		return new SqlStringBuilder(100)
		.append("(SELECT ").append(member(b.view(view), "*")).append(SCOMA).append(column.sqlWithTag(b))
		.append(" FROM ").append(view.sqlWithTag(b)).append(")")
		.toString();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue()); 
	}

	public static DBColumn windowColumn(TaggableView view, TaggableColumn column) {
		var wv = new WindowView(view, column).as(view.tagname()); 
		return new DBColumn() {
			@Override
			public String sql(QueryParameterBuilder builder) { //overwrite view
				return member(builder.overwriteView(wv), doubleQuote(column.tagname()));
			}
			@Override
			public JavaType getType() {
				return column.getType();
			}
			@Override
			public String toString() {
				return sql(addWithValue());
			}
		};
	}
}
