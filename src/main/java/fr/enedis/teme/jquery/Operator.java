package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.illegalArgumentIfNot;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum Operator {
	
	EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">="), 
	LIKE, NOT_LIKE, IN, NOT_IN, IS_NULL, IS_NOT_NULL;
	
	private final String sign;

	private Operator() {
		this.sign = null;
	}

	public String sql(Object o, ParameterHolder arg) {
		if(sign != null) {
			return sign + arg.appendNullableParameter(o);
		}
		var fn = " " + toString();
		if(this == LIKE || this == NOT_LIKE) { 
			return fn + " " + arg.appendString(o); //varchar
		}
		if(this == IN || this == NOT_IN) {
			return fn + "(" + arg.appendArray(o) + ")";
		}
		illegalArgumentIfNot(o == null, ()-> "unexpected param " + o);
		return fn;
	}
	
	@Override
	public String toString() {
		return sign == null ? name().replace("_", " ") : sign;
	}
}