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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.JavaType.Typed;
import org.usf.jquery.core.ViewQuery;

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
				return matchTypes((DBColumn) jqueryArgParser(COLUMN).parseEntry(entry, td), types); //only with JDBC types
			} catch (EvalException e) {/*do not throw exception*/}
			try {
				return matchTypes((ViewQuery) jqueryArgParser(QUERY).parseEntry(entry, td), types); //only with JDBC types
			} catch (EvalException e) {/*do not throw exception*/}
			if(isEmpty(types)) {
				return jdbcArgParser(null).parseEntry(entry, td);
			}
		}
		for(var type : types) {
			try {
				if(type instanceof JQueryType) {
					return jqueryArgParser((JQueryType) type).parseEntry(entry, td);
				}
				else if(type instanceof JDBCType) {
					return jdbcArgParser((JDBCType) type).parseEntry(entry, td);
				}
				else {
					throw unsupportedTypeException(type);
				}
			} catch (ParseException | EvalException e) {/*do not throw exception*/} // only parseException
		}
		throw badArgumentTypeException(types, entry.toString());
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
	
	private static Object parseUnknown(String s) {
		for(var p : STD_PRS) {
			try {
				return p.parseValue(s);
			} catch(ParseException e) {/*do not throw exception*/}
		}
		return s;
	}
	
	private static Object matchTypes(Typed o, JavaType... types) { // only jdbcType
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
	
	private static UnsupportedOperationException unsupportedTypeException(JavaType type) {
		return new UnsupportedOperationException("unsupported type " + type.toString());
	}
}
