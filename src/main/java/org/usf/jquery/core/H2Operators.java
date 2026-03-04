package org.usf.jquery.core;


import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Parameter.required;

/**
 * 
 * @author u$f
 * 
 */
public class H2Operators extends Operators {

	@Override
	public TypedOperator bitAnd() {
		return new TypedOperator(BIGINT, function("BITAND"), required(BIGINT), required(BIGINT));
	}

	@Override
	public TypedOperator bitOr() {
		return new TypedOperator(BIGINT, function("BITOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public TypedOperator bitXor() {
		return new TypedOperator(BIGINT, function("BITXOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public TypedOperator bitNot() {
		return new TypedOperator(BIGINT, function("BITNOT"), required(BIGINT));
	}

	@Override
	public TypedOperator bitShiftLeft() {
		return new TypedOperator(BIGINT, function("LSHIFT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public TypedOperator bitShiftRight() {
		return new TypedOperator(BIGINT, function("RSHIFT"), required(BIGINT), required(INTEGER));
	}
	
}
