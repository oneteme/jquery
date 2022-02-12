package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.SPACE_SEPARATOR;
import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum CompareOperator {
	
	EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">="), 
	LIKE, NOT_LIKE, IN, NOT_IN, IS_NULL, IS_NOT_NULL;
	
	private final String sign;

	private CompareOperator() {
		this.sign = null;
	}

	public String sql(Object o, QueryParameterBuilder arg) {
		if(sign != null) {
			return sign + arg.appendNullableParameter(o);
		}
		var fn = SPACE_SEPARATOR + toString();
		if(this == LIKE || this == NOT_LIKE) { 
			return fn + SPACE_SEPARATOR + arg.appendString(o); //String only
		}
		if(this == IN || this == NOT_IN) {
			return fn + "(" + arg.appendArray(o) + ")";
		}
		illegalArgumentIf(o != null, ()-> "unexpected param " + o);
		return fn;
	}
	
	@Override
	public String toString() {
		return sign == null ? name().replace("_", SPACE_SEPARATOR) : sign;
	}
}