package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class TableColumn implements TaggableColumn {

	@NonNull
	private final String columnName;
	@NonNull
	private final String reference;
	private final String tableRef;

	@Override
	public String sql(QueryParameterBuilder arg) {
		return arg.columnFullReference(tableRef, columnName);
	}

	@Override
	public String tagname() {
		return reference;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
