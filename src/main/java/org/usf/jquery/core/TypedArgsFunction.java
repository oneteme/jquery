package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class TypedArgsFunction implements DBFunction {
	
	private final String name;
	private final boolean aggregate;
	private final List<BiFunction<QueryParameterBuilder, Object, String>> appenders;
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		int n = appenders.size();
		illegalArgumentIf(!hasSize(args, n), ()-> name() + " function takes " + n + " parameters");
		return DBFunction.super.sql(builder, args);
	}

	@Override
	public String name() {
		return name;
	}
	
	@Override
	public boolean isAggregation() {
		return aggregate;
	}
	
	@Override
	public String appendParameters(QueryParameterBuilder builder, Object[] args) {
		return IntStream.range(0, appenders.size())
			.mapToObj(i-> appenders.get(i).apply(builder, args[i]))
			.collect(joining(SCOMA));		
	}
}