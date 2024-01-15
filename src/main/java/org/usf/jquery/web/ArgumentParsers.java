package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JqueryType.COLUMN;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.ParseException.cannotParseException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.JqueryType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentParsers {
	
	private static final JDBCArgumentParser[] STD_PRS = {
			jdbcArgParser(BIGINT), jdbcArgParser(DOUBLE), 
			jdbcArgParser(DATE), jdbcArgParser(TIMESTAMP), 
			jdbcArgParser(TIME), jdbcArgParser(TIMESTAMP_WITH_TIMEZONE)};
	
	public static Object parse(RequestEntryChain entry, TableDecorator td, JavaType... types) {
		if(isEmpty(types) || Stream.of(types).allMatch(JDBCType.class::isInstance)) {
			try {
				return jqueryArgParser(COLUMN).parse(entry, td); //only JDBC types
			} catch (Exception e) {/*do not throw exception*/}
			if(isEmpty(types)) {
				return jdbcArgParser(null).parse(entry, td);
			}
		}
		for(var type : types) {
			try {
				if(type instanceof JqueryType) {
					return jqueryArgParser((JqueryType) type).parse(entry, td);
				}
				else if(type instanceof JDBCType) {
					return jdbcArgParser((JDBCType) type).parse(entry, td);
				}
				else {
					throw new UnsupportedOperationException("unsupported " + type);
				}
			} catch (Exception e) {/*do not throw exception*/}
		}
		throw cannotParseException("value", entry.toString());
	}

	public static JDBCArgumentParser jdbcArgParser(JDBCType type) {
		if(isNull(type)) {
			return ArgumentParsers::parseUnknown;
		}
		switch (type) {
		case BOOLEAN: 					return Boolean::parseBoolean;
		case BIT: 						return Boolean::parseBoolean;
		case TINYINT: 					return Byte::parseByte;
		case SMALLINT:					return Short::parseShort;
		case INTEGER: 					return Integer::parseInt;
		case BIGINT: 					return Long::parseLong;
		case REAL: 						return Float::parseFloat;
		case FLOAT: 					return Double::parseDouble;
		case DOUBLE: 					return Double::parseDouble;
		case NUMERIC: 					return BigDecimal::new;
		case DECIMAL: 					return BigDecimal::new;
		case CHAR: 						return v-> v;
		case VARCHAR: 					return v-> v;
		case NVARCHAR: 					return v-> v;
		case LONGNVARCHAR: 				return v-> v;
		case DATE: 						return v-> Date.valueOf(LocalDate.parse(v));
		case TIME: 						return v-> Time.valueOf(LocalTime.parse(v));
		case TIMESTAMP: 				return v-> Timestamp.from(Instant.parse(v));
		case TIMESTAMP_WITH_TIMEZONE:	return v-> Timestamp.from(ZonedDateTime.parse(v).toInstant());
		case OTHER:
		default: 						throw unsupportedTypeException(type);
		}
	}

	public static JavaArgumentParser jqueryArgParser(@NonNull JqueryType type) {
		switch (type) {
		case COLUMN:	return RequestEntryChain::asColumn;
		case ORDER : 	return RequestEntryChain::asOrder;
		case FILTER: 	return RequestEntryChain::asFilter;
		case CLAUSE:	return RequestEntryChain::asOperation;
		default:		throw unsupportedTypeException(type);
		}
	}

	private static Object parseUnknown(String s) {
		for(var p : STD_PRS) {
			try {
				return p.parse(s);
			} catch (Exception e) {/*do not throw exception*/}
		}
		return s;
	}
	
	private static UnsupportedOperationException unsupportedTypeException(JavaType type) {
		return new UnsupportedOperationException("unsupported type " + type.name());
	}
}
