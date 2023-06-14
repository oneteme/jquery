package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;

import java.util.Collection;
import java.util.LinkedList;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class CaseColumn implements DBColumn {

	private final Collection<WhenExpression> expressions = new LinkedList<>();
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return "CASE " + expressions.stream().map(o-> o.sql(builder)).collect(joining(SPACE)) + " END";
	}
		
	public CaseColumn append(WhenExpression we) {
		expressions.add(we);
		return this;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}

	public static CaseColumn caseWhen(DBFilter filter, Object value){
		return new CaseColumn()
				.append(new WhenExpression(filter, value));
	}
}
