package org.usf.jquery.core;


import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Parameter.required;

/**
 * 
 * @author u$f
 * 
 */
public class TeradataOperators extends Operators {

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
		return new TypedOperator(BIGINT, function("SHIFTLEFT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public TypedOperator bitShiftRight() {
		return new TypedOperator(BIGINT, function("SHIFTRIGHT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public TypedOperator week() {
		return new TypedOperator(INTEGER, function("TD_WEEK_OF_YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	@Override
	public TypedOperator dow() {
		return new TypedOperator(INTEGER, function("TD_DAY_OF_WEEK"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	@Override
	public TypedOperator doy() {
		return new TypedOperator(INTEGER, function("TD_DAY_OF_YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	@Override
	public TypedOperator replace() {
		return new TypedOperator(VARCHAR, function("OREPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR));
	}
}
