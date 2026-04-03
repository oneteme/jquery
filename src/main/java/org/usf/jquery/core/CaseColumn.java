package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;

import java.util.Objects;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class CaseColumn implements Column {

	private final WhenCase[] whenCases;
	
	@Override
	public int compose(QueryDeclaration declare) {
		return declare.composeNestedOrElse(whenCases, this);
	}

	@Override
	public void build(QueryBuilder query) {
		query.withValue() //append filters literally 
		.append("CASE ").appendEach(SPACE, whenCases).append(" END");
	}
	
	@Override
	public JDBCType getType() {
		return Stream.of(whenCases)
				.map(WhenCase::getType)
				.filter(Objects::nonNull) // should have same type
				.findAny().orElse(null);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
