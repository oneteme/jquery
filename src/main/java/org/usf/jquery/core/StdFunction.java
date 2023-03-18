package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.util.function.BiFunction;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StdFunction implements DBFunction {
	 
	//numeric functions
	ABS(QueryParameterBuilder::appendNumber), 
	SQRT(QueryParameterBuilder::appendNumber), 
	TRUNC(QueryParameterBuilder::appendNumber), 
	CEIL(QueryParameterBuilder::appendNumber), 
	FLOOR(QueryParameterBuilder::appendNumber),
	//varchar functions
	TRIM(QueryParameterBuilder::appendString), 
	LENGTH(QueryParameterBuilder::appendString), //return number
	UPPER(QueryParameterBuilder::appendString), 
	LOWER(QueryParameterBuilder::appendString);
	
	private final BiFunction<QueryParameterBuilder, Object, String> fn;

	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null || !isEmpty(args),  ()-> this.name() + " require one parameter");
		return this.name() + parenthese(fn.apply(ph, operand));
	}
}
