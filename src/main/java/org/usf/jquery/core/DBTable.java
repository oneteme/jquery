package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.member;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class DBTable implements DBView {
	
	private final String id;
	private final String name;

	@Override
	public String sql(QueryParameterBuilder builder) {
		return member(builder.getSchema(), name);
	}

	@Override
	public String id() {
		return id;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
