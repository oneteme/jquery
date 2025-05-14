package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireLegalVariable;

import lombok.Getter;
import lombok.With;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class TableView implements DBView {
	
	@Getter
	private final String name;
	private final String schema; //optional
	private final String tag; //optional
	@With
	private final Adjuster<String> adjuster; //view name adjuster

	public TableView(String name, String schema, String tag) {
		this(name, schema, tag, null);
	}

	public TableView(String name, String schema, String tag, Adjuster<String> adjuster) {
		this.name = requireLegalVariable(name);
		this.schema = nonNull(schema) ? requireLegalVariable(schema) : null;
		this.tag = tag;
		this.adjuster = adjuster;
	}
	
	@Override
	public void build(QueryBuilder query) {
		var sch = getSchemaOrElse(query.getSchema());
		if(nonNull(sch)) {
			query.append(sch + '.');
		}
		query.append(nonNull(adjuster) ? adjuster.build(query, name) : name); 
	}
	
	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //schema priority order
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
