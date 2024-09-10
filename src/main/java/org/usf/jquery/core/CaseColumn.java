package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.QueryContext.addWithValue;
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
	public String sql(QueryContext ctx) {
		var sub = ctx.withValue(); //force literal parameter
		return Stream.of(whenCases)
		.map(o-> o.sql(sub))
		.collect(joining(SPACE, "CASE ", " END"));
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
		return sql(addWithValue());
	}
}
