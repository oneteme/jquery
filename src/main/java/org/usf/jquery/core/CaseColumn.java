package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Objects;
import java.util.function.Consumer;
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
	public void build(QueryBuilder query) {
		query.withValue() //append filters literally 
		.append("CASE ").append(SPACE, whenCases).append(" END");
	}
	
	@Override
	public JDBCType getType() {
		return Stream.of(whenCases)
				.map(WhenCase::getType)
				.filter(Objects::nonNull) // should have same type
				.findAny().orElse(null);
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return Nested.aggregation(query, groupKeys, this, whenCases);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
