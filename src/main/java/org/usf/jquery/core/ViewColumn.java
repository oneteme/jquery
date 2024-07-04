package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.member;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class ViewColumn implements TaggableColumn {

	private final DBView view;
	private final String name;
	private final String tag;
	private final JavaType type;
	
	public ViewColumn(String name, String tag) {
		this(null, name, tag, null);
	}
	
	public ViewColumn(DBView view, String name, String tag) {
		this(view, name, tag, null);
	}

	@Override
	public String sql(QueryParameterBuilder arg) {
		return nonNull(view) ? member(arg.view(view), name) : name;
	}
	
	@Override
	public JavaType getType() {
		return type;
	}

	@Override
	public String tagname() {
		return tag;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
