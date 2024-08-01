package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public class TableView implements DBView {
	
	private final String schema;
	private final String name;

	public TableView(String name) {
		this(name, null); //use tablename as id
	}

	@Override
	public String sql(QueryParameterBuilder builder) {
		return member(getSchemaOrElse(builder.getSchema()), name);
	}

	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //priority order
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
