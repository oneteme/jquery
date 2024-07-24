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
	
	private final String id;
	private final String schema;
	private final String name;

	public TableView(String name) {
		this(name, null, name); //use tablename as id
	}

	public TableView(String schema, String name) {
		this(name, schema, name); //use tablename as id
	}

	@Override
	public String sql(QueryParameterBuilder builder) {
		var scm = nonNull(schema) ? schema : builder.getSchema(); //priority order
		return member(scm, name);
	}

	@Override
	public String id() {
		return id;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
