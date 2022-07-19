package org.usf.jquery;

import static org.usf.jquery.DBColumn.ofReference;
import static org.usf.jquery.SqlStringBuilder.parenthese;
import static org.usf.jquery.Utils.isEmpty;
import static org.usf.jquery.Validation.illegalArgumentIf;

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
