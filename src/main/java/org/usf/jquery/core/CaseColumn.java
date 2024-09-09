package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class CaseColumn implements DBColumn { // TD override isAggregation

	private final Collection<WhenCase> whenCases = new ArrayList<>(); //immutable
	
	@Override
	public String sql(QueryVariables builder) {
		var b = builder.withValue(); //force literal parameter
		return whenCases.stream() //empty !? 
		.map(o-> o.sql(b))
		.collect(joining(SPACE, "CASE ", " END"));
	}
	
	@Override
	public JDBCType getType() {
		return whenCases.stream()
				.map(WhenCase::getType)
				.filter(Objects::nonNull) // should have same type
				.findAny()
				.orElse(null);
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) {
		var res = false;
		for(var c : whenCases) {
			res |= c.resolve(builder);
		}
		return res;
	}
	
	@Override
	public void views(Collection<DBView> views) {
		for(var e : whenCases) {
			e.views(views);
		}
	}
		
	public CaseColumn append(WhenCase we) {
		whenCases.add(we);
		return this;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

	public static CaseColumn caseWhen(DBFilter filter, Object value){
		return new CaseColumn()
				.append(new WhenCase(filter, value));
	}
}
