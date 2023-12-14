package org.usf.jquery.web;

import static java.util.Objects.isNull;
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

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ArgumentParser {
	
	Object nativeParse(String v);

	default Object parse(String v) {
		try {
			return nativeParse(v);
		}
		catch(Exception e) {
			throw cannotParseException("parameter value", v, e);
		}
	}

	default Object[] parseAll(String... args) {
		return isNull(args) ? null : Stream.of(args).map(this::parse).toArray();
	}
	
	static ArgumentParser parserFor(JDBCType type) {
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
		default: 						throw new UnsupportedOperationException("unsupported type " + type);
		}
	}
	
}
