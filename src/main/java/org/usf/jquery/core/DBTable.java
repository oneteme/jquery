package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class DBTable implements TaggableView {
	
	private final String name;
	private final String tag;
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return name;
	}
	
	@Override 
	public String tagname() {
		return tag;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}	
}
