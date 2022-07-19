package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.ofReference;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

public enum AggregatFunction implements DBFunction {
	
	COUNT, SUM, AVG, MIN, MAX;
	
	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null || !isEmpty(args),  ()-> this.name() + " require one parameter");
		if(this == COUNT || this == MIN || this == MAX) {
			return this.name() + parenthese(ph.appendParameter(operand));
		}
		else if(this == SUM || this == AVG) {
			return this.name() + parenthese(ph.appendNumber(operand));
		}
		throw new UnsupportedOperationException("Unsupported operator " + this.name());
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
