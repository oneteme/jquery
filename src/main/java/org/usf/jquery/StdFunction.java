package org.usf.jquery;

import static org.usf.jquery.SqlStringBuilder.parenthese;
import static org.usf.jquery.Utils.isEmpty;
import static org.usf.jquery.Validation.illegalArgumentIf;

public enum StdFunction implements DBFunction {

	ABS, SQRT, TRUNC, CEIL, FLOOR, //numeric functions
	LENGTH, TRIM, UPPER, LOWER; //string functions
	
	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null || !isEmpty(args),  ()-> this.name() + " require one parameter");
		if(this == ABS || this == SQRT || this == TRUNC || this == CEIL || this == FLOOR) {
			return this.name() + parenthese(ph.appendNumber(operand));
		}
		else if(this == LENGTH || this == TRIM || this == UPPER || this == LOWER) {
			return this.name() + parenthese(ph.appendString(operand));
		}
		throw new UnsupportedOperationException("Unsupported operator " + this.name());
	}
}
