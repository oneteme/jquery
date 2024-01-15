package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
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
public final class WindowView implements TaggableView {
	
	private final TaggableView view;
	private final TaggableColumn column; //named operation column
	
	@Override
	public String sql(QueryParameterBuilder builder, String schema) { //sub query should not use main builder
		var b = addWithValue("w");
		return new SqlStringBuilder(100)
		.append("(SELECT ").append(member(b.view(view), "*")).append(", ").append(column.sqlWithTag(b))
		.append(" FROM ").append(view.sqlWithTag(b, schema)).append(")")
		.toString();
	}
	
	@Override
	public String tagname() { //inherits tagname
		return view.tagname();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), "<schema>"); 
	}
	

	public static DBColumn windowColumn(TaggableView view, TaggableColumn column) {
		var wv = new WindowView(view, column);
		return new DBColumn() {
			@Override
			public String sql(QueryParameterBuilder builder) { //overwrite view
				return member(builder.overwriteView(wv), doubleQuote(column.tagname()));
			}
			@Override
			public JavaType javaType() {
				return column.javaType();
			}
			@Override
			public String toString() {
				return sql(addWithValue());
			}
		};
	}
}
