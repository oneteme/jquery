package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.member;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@EqualsAndHashCode(of = "tag")
@RequiredArgsConstructor
public class DBTable implements DBView {
	
	private final String name;
	private final String tag; //only for equals

	public DBTable(String name) {
		this(name, null);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return member(builder.getSchema(), name);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
