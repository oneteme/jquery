package org.usf.jquery;

import static org.usf.jquery.QueryParameterBuilder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NamedColumn implements TaggableColumn {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final String tagName;

	@Override
	public String sql(QueryParameterBuilder arg) {
		return column.sql(arg);
	}
	
	@Override
	public boolean isAggregation() {
		return column.isAggregation();
	}

	@Override
	public boolean isConstant() {
		return column.isConstant();
	}

	@Override
	public String tagname() {
		return tagName;
	}

	@Override
	public NamedColumn as(String name) { // map
		return new NamedColumn(this.column, name);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
