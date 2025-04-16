package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireLegalVariable;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class TableView implements DBView, Driven<TableView, String> {
	
	private final String name;
	private final String schema; //optional
	private final String tag; //optional
	private final Adjuster<String> adjsuter; //optional

	public TableView(String name, String schema, String tag, Adjuster<String> adjsuter) {
		this.name = requireLegalVariable(name);
		this.schema = requireLegalVariable(schema);
		this.tag = tag;
		this.adjsuter = adjsuter;
	}

	public TableView(String name, String schema, String tag) {
		this(name, schema, tag, null);
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
