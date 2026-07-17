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
public final class Table implements View {
	
	private final String name;
	private final String schema; //optional

	public Table(String name, String schema) {
		this.name = requireLegalVariable(name);
		this.schema = nonNull(schema) ? requireLegalVariable(schema) : null;
	}
	
	@Override
	public void build(SqlBuilder builder) {
		var sch = getSchemaOrElse(builder.getStore().name());
		if(nonNull(sch)) {
			builder.append(sch + '.');
		}
		builder.append(name); 
	}
	
	public String getSchemaOrElse(String defaultSchema) {
		return nonNull(schema) ? schema : defaultSchema; //schema priority order
	}

	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
