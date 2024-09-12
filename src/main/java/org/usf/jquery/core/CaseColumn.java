package org.usf.jquery.core;

import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public final class CaseColumn implements DBColumn {

	private final WhenCase[] whenCases;
	
	CaseColumn(WhenCase[] whenCases) {
		this.whenCases = requireAtLeastNArgs(1, whenCases, CaseColumn.class::getSimpleName);
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		var sub = ctx.withValue(); //force literal parameter
		sb.runForeach(whenCases, SPACE, o-> o.sql(sb, sub), "CASE ", " END");
	}
	
	@Override
	public JDBCType getType() {
		return Stream.of(whenCases)
				.map(WhenCase::getType)
				.filter(Objects::nonNull) // should have same type
				.findAny().orElse(null);
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) {
		return resolveAll(whenCases, builder);
	}
	
	@Override
	public void views(Collection<DBView> views) {
		for(var e : whenCases) {
			e.views(views);
		}
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
