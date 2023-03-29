package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.POINT;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TableColumn implements TaggableColumn {

	@NonNull
	private final String columnName;
	@NonNull
	private final String reference;
	private final String tableRef;

	@Override
	public String sql(QueryParameterBuilder arg) {
		return tableRef + POINT + columnName;
	}

	@Override
	public String reference() {
		return reference;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
