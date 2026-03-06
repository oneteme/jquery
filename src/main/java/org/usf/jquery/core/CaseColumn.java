package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public final class CaseColumn implements Column {

	private final WhenCase[] whenCases;

	public CaseColumn(WhenCase... cases) {
		this.whenCases = cases;
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		return DBObject.composeNested(query, groupKeys, this, whenCases);
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
	
	public CaseColumn when(WhenCase... cases) {
		if(isEmpty(cases)) {
			return this;
		}
		if(isEmpty(this.whenCases)) {
			return new CaseColumn(cases);
		}
		return new CaseColumn(Stream.concat(Stream.of(this.whenCases), Stream.of(cases)).toArray(WhenCase[]::new));
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
