package org.usf.jquery.web;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JqueryType.COLUMN;
import static org.usf.jquery.core.Utils.isEmpty;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.JqueryType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentParsers {
	
	private static final JavaType[] STD_TYPES = {BIGINT, DOUBLE, DATE, TIMESTAMP, TIME, TIMESTAMP_WITH_TIMEZONE, VARCHAR};
	
	public static Object parse(RequestEntryChain entry, TableDecorator td, JavaType... types) {
		if(isEmpty(types)) {
			types = STD_TYPES;
		}
		else {
			if(types.length == 1 && types[0] instanceof JqueryType) { // support single JQuery type 
				return jqueryArgParser((JqueryType)types[0]).parse(entry, td);
			}
			if(!Stream.of(types).allMatch(JDBCType.class::isInstance)) {
				throw new IllegalArgumentException("different types");
			}
		}
		var res = tryParse(entry, td, jqueryArgParser(COLUMN));
		if(res.isPresent()) {
			return res.get();
		}
		for(var type : types) {
			res = tryParse(entry, td, jdbcArgParser((JDBCType) type));
			if(res.isPresent()) {
				return res.get();
			}
		}
		throw new ParseException("cannot parse value : " + entry);
	}

	public static JDBCArgumentParser jdbcArgParser(JDBCType type) {
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
		default: 						throw unsupportedTypeException(requireNonNull(type));
		}
	}

	public static JavaArgumentParser jqueryArgParser(JqueryType type) {
		switch (type) {
		case COLUMN:	return RequestEntryChain::asColumn;
		case ORDER : 	return RequestEntryChain::asOrder;
		case CLAUSE:	return RequestEntryChain::asOperation;
		default:		throw unsupportedTypeException(requireNonNull(type));
		}
	}
	
	private static Optional<Object> tryParse(RequestEntryChain entry, TableDecorator td, JavaArgumentParser p) {
		try {
			return Optional.of(p.parse(entry, td));
		}
		catch(Exception e) {
			return empty(); 
		}
	}
	
	private static UnsupportedOperationException unsupportedTypeException(JavaType type) {
		return new UnsupportedOperationException("unsupported type " + type.name());
	}
}
