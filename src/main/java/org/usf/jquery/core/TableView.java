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
	public void build(QueryBuilder query) {
		var sch = getSchemaOrElse(query.getSchema());
		(nonNull(sch) ? query.append(sch + '.') : query).append(name);
	}

	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //schema priority order
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
