package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.BadArgumentException.badArgumentTypeException;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JQueryType.COLUMN;
import static org.usf.jquery.core.JQueryType.QUERY;
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
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.JavaType.Typed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentParsers {
	
	private static final JDBCType[] STD_TYPES = {
			BIGINT, DOUBLE, DATE, TIMESTAMP, 
			TIME, TIMESTAMP_WITH_TIMEZONE };

	public static Object parse(RequestEntryChain entry, TableDecorator td, JavaType... types) {
		if(isEmpty(types) || Stream.of(types).allMatch(o-> o.getClass() == JDBCType.class)) {
			return parseJdbc(entry, td, cast(types, JDBCType[].class, JDBCType[]::new));
		}
		if(Stream.of(types).allMatch(o-> o.getClass() == JQueryType.class)) {
			return parseJQuery(entry, td, cast(types, JQueryType[].class, JQueryType[]::new));
		}
		throw new UnsupportedOperationException("unsupported types " + Arrays.toString(types));
	}

	public static Object parseJdbc(RequestEntryChain entry, TableDecorator td, JDBCType... types) {
		try {
			return matchTypes((Typed) parseJQuery(entry, td, COLUMN, QUERY), types); //try parse column | query first
		} catch (ParseException e) {/*do not throw exception*/}
		if(isEmpty(types)) {
			types = STD_TYPES;
		}
		for(var type : types) {
			try {
				return jdbcArgParser(type).parseEntry(entry, td);
			} catch (ParseException e) { /*do not throw exception*/
				log.trace("parse {} : '{}' => {}", type, entry, e.getMessage());
			}
		}
		throw cannotParseException(entry.toString(), Arrays.toString(types));
	}
	
	public static Object parseJQuery(RequestEntryChain entry, TableDecorator td, JQueryType... types) {
		for(var type : types) {
			try {
				return jqueryArgParser(type).parseEntry(entry, td);
			} catch (ParseException e) {/*do not throw exception*/
				log.trace("parse {} : '{}' => {}", type, entry, e.getMessage());
			} 
		}
		throw cannotParseException(entry.toString(), Arrays.toString(types));
	}
	
	public static JDBCArgumentParser jdbcArgParser(@NonNull JDBCType type) {
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

	public static JavaArgumentParser jqueryArgParser(@NonNull JQueryType type) {
		switch (type) {
		case COLUMN:	return RequestEntryChain::evalColumn;
		case FILTER: 	return RequestEntryChain::evalFilter;
		case ORDER: 	return RequestEntryChain::evalOrder;
		case QUERY: 	return RequestEntryChain::evalQuery;
		case PARTITION: return RequestEntryChain::evalPartition;
		default:		throw unsupportedTypeException(type);
		}
	}
	
	private static Object matchTypes(Typed o, JDBCType... types) {
		if(isNull(o.getType()) || isEmpty(types)) {
			return true;
		}
		for(var t : types) {
			if(t.accept(o)) {
				return o;
			}
		}
		throw badArgumentTypeException(types, o);
	}
	
	private static <T extends JavaType> T[] cast(JavaType[] types, Class<T[]> c, IntFunction<T[]> fn) {
		return types.getClass() == c ? c.cast(types) : Stream.of(types).toArray(fn);
	}
	
	private static UnsupportedOperationException unsupportedTypeException(JavaType type) {
		return new UnsupportedOperationException("unsupported type " + type.toString());
	}
}
