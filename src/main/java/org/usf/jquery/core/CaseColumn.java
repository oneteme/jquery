package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

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
		return expressions.stream()
		.map(o-> o.sql(builder.withValue()))
		.collect(joining(SPACE, "CASE ", " END"));
	}
	
	@Override
	public JavaType javaType() {
		return expressions.stream()
				.map(JDBCType::typeOf)
				.filter(Objects::nonNull) // should have same type
				.findAny().orElse(null);
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
