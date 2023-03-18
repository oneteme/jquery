package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.ofReference;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

import java.util.function.BiFunction;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AggregatFunction implements DBFunction {
	
	COUNT(QueryParameterBuilder::appendNumber), 
	SUM(QueryParameterBuilder::appendNumber), 
	AVG(QueryParameterBuilder::appendNumber), 
	MIN(QueryParameterBuilder::appendParameter), //String, Number, Date, ..
	MAX(QueryParameterBuilder::appendParameter);

	private final BiFunction<QueryParameterBuilder, Object, String> fn;
	
	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null || !isEmpty(args), ()-> this.name() + " require one parameter");
		return this.name() + parenthese(fn.apply(ph, operand));
	}
		
	@Override
	public boolean isAggregate() {
		return true;
	}
	
	public FunctionColumn ofAll() {
		illegalArgumentIf(this != COUNT, ()-> "column is required");
		return new FunctionColumn(this, ofReference("*"));
	}
}
