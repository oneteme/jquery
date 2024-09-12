package org.usf.jquery.core;

import static java.util.Objects.nonNull;

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
	
	private final String schema; //optional
	private final String name;
	private final String tag; //optional

	public TableView(String schema, String name) {
		this(schema, name, name);
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.appendIfNonNull(getSchemaOrElse(ctx.getSchema()), v-> v + '.').append(name);
	}

	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //schema priority order
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
