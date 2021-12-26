package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.OperationExpression.join;
import static fr.enedis.teme.jquery.SqlStringBuilder.constantString;
import static fr.enedis.teme.jquery.SqlStringBuilder.toSqlString;
import static fr.enedis.teme.jquery.Utils.isArray;
import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;
import static fr.enedis.teme.jquery.Validation.illegalArgumentIfNot;
import static java.lang.reflect.Array.getLength;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum Operator {
	
	EQ("="), NE("<>"), LT("<"), LE("<="), GT(">"), GE(">="), 
	IS_NULL, IS_NOT_NULL, LIKE, NOT_LIKE, IN, NOT_IN;
	
	private static final String ARG = "?";
	private final String sign;

	private Operator() {
		this.sign = null;
	}

	public String sql(Object o, boolean dynamic) {
		if(sign != null) {
			if(o != null) {
				illegalArgumentIf(isArray(o), "unexpected array param " + o);
			}
			return sign + (dynamic ? ARG : toSqlString(o));
		}
		var fn = " " + toString();
		if(this == IS_NULL || this == IS_NOT_NULL) {
			illegalArgumentIfNot(o == null, ()-> "unexpected param " + o);
			return fn;
		}
		if(this == LIKE || this == NOT_LIKE) { 
			illegalArgumentIfNot(o instanceof String, "expected string param");
			return fn + " " + (dynamic ? ARG : constantString(o)); //varchar
		}
		if(this == IN || this == NOT_IN) {
			illegalArgumentIfNot(isArray(o), "expected array param");
			var values = dynamic ? nParameter(getLength(o)) : join(o);
			return fn + "(" + values + ")";
		}
		throw new IllegalStateException();
	}
	
	@Override
	public String toString() {
		return sign == null ? name().replace("_", " ") : sign;
	}

	static String nParameter(int n){
        return n == 1 ? ARG : ARG + ",?".repeat(n-1);
    }
}