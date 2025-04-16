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
public final class TableView implements DBView, Driven<TableView, String> {
	
	private final String schema; //optional
	private final String name;
	private final String tag; //optional
	private final Adjuster<String> adjsuter;

	public TableView(String schema, String name, String tag) {
		this(schema, name, tag, null);
	}

	@Override
	public void build(QueryBuilder query) {
		var sch = getSchemaOrElse(query.getSchema());
		if(nonNull(sch)) {
			query.append(sch + '.');
		}
		query.append(name, adjsuter); 
	}
	
	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //schema priority order
	}
	
	@Override
	public TableView adjuster(Adjuster<String> adjsuter) {
		return new TableView(schema, name, tag, adjsuter);
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
