package org.usf.jquery.core;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Validation.requireNArgs;

import java.util.List;

import org.usf.jquery.core.QueryParameterBuilder.Appender;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class TypedFunction implements DBFunction {
	
	private final String name;
	private final boolean aggregate;
	private final List<Appender> appenders;
	@Getter
	private final int returnedType;
	//n optional parameter 
	
	private String prefix;
	private String suffix;

	public TypedFunction(String name, boolean aggregate, Appender appender) {
		this(name, aggregate, appender, AUTO_TYPE); //TODO global variable
	}
	
	public TypedFunction(String name, boolean aggregate, Appender appender, int returnedType) {
		this(name, aggregate, singletonList(appender), returnedType);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(appenders.size(), args, ()-> "function " + name());
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
		return range(0, appenders.size())
			.mapToObj(i-> appenders.get(i).append(builder, args[i]))
			.collect(joining(SCOMA, requireNonNullElse(prefix, ""), requireNonNullElse(suffix, "")));		
	}
	
	public TypedFunction argsPrefix(String prefix){
		this.prefix = prefix;
		return this;
	}
		
	public TypedFunction argsSuffix(String suffix){
		this.suffix = suffix;
		return this;
	}
}