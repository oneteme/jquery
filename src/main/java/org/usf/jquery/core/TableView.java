package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryContext.addWithValue;
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
	private final String tag;

	public TableView(String schema, String name) {
		this(schema, name, name);
	}

	@Override
	public String sql(QueryContext ctx) {
		return member(getSchemaOrElse(ctx.getSchema()), name);
	}

	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //schema priority order
	}

	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
