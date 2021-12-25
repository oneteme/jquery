package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.constantString;
import static fr.enedis.teme.jquery.SqlStringBuilder.toSqlString;
import static java.lang.reflect.Array.getLength;
import static java.util.Objects.requireNonNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum Operator {
	
	LT("<"), LE("<="), GT(">"), GE(">="), EQ("="), NE("<>"), 
	IS_NULL, IS_NOT_NULL, LIKE, NOT_LIKE, IN, NOT_IN;
	
	private final String sign;

	private Operator() {
		this.sign = null;
	}

	public String toSql(Object o, boolean dynamic) {
		if(sign != null) {
			return sign + (dynamic ? nParameter(1): toSqlString(requireNonNull(o))); //check types
		}
		var fn = " " + name().replace("_", " ");
		if(this == IS_NULL || this == IS_NOT_NULL) {
			return fn;
		}
		if(this == LIKE || this == NOT_LIKE) { 
			return fn + " " + (dynamic ? nParameter(1) : constantString(o)); //varchar
		}
		if(this == IN || this == NOT_IN) {
			var values = dynamic ? nParameter(getLength(o)) : OperationExpression.join(o);
			return fn + "(" + values + ")";
		}
		throw new UnsupportedOperationException();
	}

	private static String nParameter(int n){
		var v = "?";
        return n == 1 ? v : v + ",?".repeat(n-1);
    }
}