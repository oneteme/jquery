package org.usf.jquery;

import static org.usf.jquery.SqlStringBuilder.SPACE_SEPARATOR;
import static org.usf.jquery.SqlStringBuilder.parenthese;
import static org.usf.jquery.SqlStringBuilder.space;
import static org.usf.jquery.Utils.hasSize;
import static org.usf.jquery.Utils.isEmpty;
import static org.usf.jquery.Validation.illegalArgumentIf;
import static org.usf.jquery.Validation.illegalArgumentIfNot;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum StdComparator implements DBComparator {
	
	EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">="), 
	LIKE, NOT_LIKE, ILIKE, NOT_ILIKE, IN, NOT_IN, IS_NULL, IS_NOT_NULL;

	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null,  ()-> this.name() + " require operand");
		if(this == IS_NULL || this == IS_NOT_NULL) {
			illegalArgumentIfNot(isEmpty(args), ()-> symbol() + " require only 1 parameter");
			return ph.appendParameter(operand) + SPACE_SEPARATOR + symbol();
		}
		if(this == IN || this == NOT_IN) {
			illegalArgumentIf(hasSize(args, s-> s<1), ()-> symbol() + " require at least 2 parameters");
			return ph.appendParameter(operand) + SPACE_SEPARATOR + symbol() + parenthese(ph.appendArray(args));  //String only
		}
		illegalArgumentIfNot(hasSize(args, 1), ()-> symbol() + " require 2 parameters");
		if(symbol != null) { //Number | String
			return ph.appendParameter(operand) + symbol + ph.appendParameter(args[0]);
		}
		if(this == LIKE || this == NOT_LIKE || this == ILIKE || this == NOT_ILIKE) { 
			return ph.appendString(operand) + space(symbol()) + ph.appendString(args[0]);  //String only
		}
		throw new UnsupportedOperationException("Unsupported operator " + this.name());
	}
	
	String symbol() {
		return symbol == null ? name().replace("_", SPACE_SEPARATOR) : symbol;
	}
	
	final String symbol;

	private StdComparator() {
		this(null);
	}
}