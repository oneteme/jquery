package org.usf.jquery.core;


import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Operators.function;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Provider.TERADATA;

/**
 * 
 * @author u$f
 * 
 */
public final class TeradataDialect extends Dialect {

	public TeradataDialect() {
		super(TERADATA);
	}

	@Override
	public OperatorDefinition bitAnd() {
		return new OperatorDefinition(BIGINT, function("BITAND"), required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitOr() {
		return new OperatorDefinition(BIGINT, function("BITOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitXor() {
		return new OperatorDefinition(BIGINT, function("BITXOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitNot() {
		return new OperatorDefinition(BIGINT, function("BITNOT"), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitShiftLeft() {
		return new OperatorDefinition(BIGINT, function("SHIFTLEFT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public OperatorDefinition bitShiftRight() {
		return new OperatorDefinition(BIGINT, function("SHIFTRIGHT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public OperatorDefinition week() {
		return new OperatorDefinition(INTEGER, function("TD_WEEK_OF_YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE)); 
	}

	@Override
	public OperatorDefinition dow() {
		return new OperatorDefinition(INTEGER, function("TD_DAY_OF_WEEK"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}

	@Override
	public OperatorDefinition doy() {
		return new OperatorDefinition(INTEGER, function("TD_DAY_OF_YEAR"), required(DATE, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE));
	}
	
	@Override
	public OperatorDefinition replace() {
		return new OperatorDefinition(VARCHAR, function("OREPLACE"), required(VARCHAR), required(VARCHAR), required(VARCHAR));
	}
}
