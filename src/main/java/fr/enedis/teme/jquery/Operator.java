package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.OperationExpression.join;
import static fr.enedis.teme.jquery.SqlStringBuilder.constantString;
import static fr.enedis.teme.jquery.SqlStringBuilder.toSqlString;
import static java.lang.reflect.Array.getLength;
import static java.util.Objects.requireNonNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum Operator {
	
	LT("<"), LE("<="), GT(">"), GE(">="), EQ("="), NE("<>"), 
	IS_NULL, IS_NOT_NULL, LIKE, NOT_LIKE, IN, NOT_IN;
	
	private static final String ARG = "?";
	private final String sign;

	private Operator() {
		this.sign = null;
	}

	public String sql(Object o, boolean dynamic) {
		if(sign != null) {
			return sign + (dynamic ? ARG : toSqlString(requireNonNull(o)));
		}
		var fn = " " + name().replace("_", " ");
		if(this == IS_NULL || this == IS_NOT_NULL) {
			return fn;
		}
		if(this == LIKE || this == NOT_LIKE) { 
			return fn + " " + (dynamic ? ARG : constantString(o)); //varchar
		}
		if(this == IN || this == NOT_IN) {
			var values = dynamic ? nParameter(getLength(o)) : join(o);
			return fn + "(" + values + ")";
		}
		throw new UnsupportedOperationException();
	}

	private static String nParameter(int n){
        return n == 1 ? ARG : ARG + ",?".repeat(n-1);
    }
}