package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
final class TypedArgsAppender implements DBFunction {
	
	private final String name;
	private final boolean aggregat;
	private final List<BiFunction<QueryParameterBuilder, Object, String>> appenders;
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		int n = appenders.size();
		illegalArgumentIf(!hasSize(args, n), ()-> name() + " operator takes " + n +" parameters");
		return DBFunction.super.sql(builder, args);
	}
	
	@Override
	public boolean isAggregate() {
		return aggregat;
	}
	
	@Override
	public String appendParameters(QueryParameterBuilder builder, Object[] args) {
		return IntStream.range(0, appenders.size())
			.mapToObj(i-> appenders.get(i).apply(builder, args[i]))
			.collect(joining(COMA));		
	}
}