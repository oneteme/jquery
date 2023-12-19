package org.usf.jquery.web;

import static org.usf.jquery.core.JDBCType.AUTO;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JavaType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * 
 * @author u$f
 * 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentParsers {
	
	private static final ArgumentParser[] DEFAULT = {
			jdbcTypeParser(BIGINT), jdbcTypeParser(DOUBLE), 
			jdbcTypeParser(DATE), jdbcTypeParser(TIMESTAMP),
			jdbcTypeParser(TIME), jdbcTypeParser(TIMESTAMP_WITH_TIMEZONE)};
	
	public static ArgumentParser javaTypeParser(JavaType type) {
		if(type instanceof JDBCType) {
			return ArgumentParsers.jdbcTypeParser((JDBCType) type);
		}
		if(type == AUTO) {
			return v-> {
				for(var p : DEFAULT) {
					var o = p.tryParse(v);
					if(o != null) {
						return o;
					}
				}
				return v;
			};
		}
		throw new UnsupportedOperationException("unsupported type " + type);
	}

	public static ArgumentParser jdbcTypeParser(JDBCType type) {
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
