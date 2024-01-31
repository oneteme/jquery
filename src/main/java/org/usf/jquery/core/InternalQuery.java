package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InternalQuery implements DBObject {
	
	private final DBColumn[] columns;
	private final DBFilter[] filters;

	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		var b = QueryParameterBuilder.addWithValue("r"); 
		return "SELECT " + b.appendLitteralArray(columns) + " FROM " + b.views().stream().map(v-> v.sqlWithTag(b, null)).collect(joining(","));
	}

}
