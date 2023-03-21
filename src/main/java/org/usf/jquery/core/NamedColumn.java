package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
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
	public String reference() {
		return tagName;
	}

	@Override
	public NamedColumn as(String name) {
		return this.column.as(name); //safe 
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
