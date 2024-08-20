package org.usf.jquery.web;

import static java.util.Collections.addAll;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JQueryType.COLUMN;
import static org.usf.jquery.core.JQueryType.QUERY_COLUMN;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.join;
import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;

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

	public static Object parse(RequestEntryChain entry, ViewDecorator td, JavaType... types) {
		List<JavaType> list = new ArrayList<>();
		if(isEmpty(types) || Stream.of(types).anyMatch(JDBCType.class::isInstance)) {
			list.add(COLUMN);
			list.add(QUERY_COLUMN);
		}
		addAll(list, isEmpty(types) ? STD_TYPES : types);
		Exception e = null;
		for(var type : list) {
			try {
				if(type instanceof JDBCType t) {
					return jdbcArgParser(t).parseEntry(entry, td);
				}
				if(type instanceof JQueryType t) {
					return jqueryArgParser(t).parseEntry(entry, td);
				}
				else {
					throw new UnsupportedOperationException(requireNonNull(type, "type is null").toString());
				}
			} catch (NoSuchResourceException | EntryParseException ex) { /*do not throw exception*/
				log.trace("parse {} : '{}' => {}", type, entry, ex.getMessage());
				e = ex;
			}
		}
		throw cannotParseEntryException(join("|", types), entry, types.length == 1 ? e : null);
	}
	
	public static JDBCArgumentParser jdbcArgParser(@NonNull JDBCType type) {
		switch (type) {
		case BOOLEAN, BIT: 				return Boolean::parseBoolean;
		case TINYINT: 					return Byte::parseByte;
		case SMALLINT:					return Short::parseShort;
		case INTEGER: 					return Integer::parseInt;
		case BIGINT: 					return Long::parseLong;
		case REAL: 						return Float::parseFloat;
		case FLOAT, DOUBLE:				return Double::parseDouble;
		case NUMERIC, DECIMAL: 			return BigDecimal::new;
		case CHAR, VARCHAR, NVARCHAR, LONGNVARCHAR: return v-> v;
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
		case QUERY_COLUMN:	return RequestEntryChain::evalQueryColumn;
		case NAMED_COLUMN:	return (e,v)-> e.evalColumn(v, true, false);
		case COLUMN:		return (e,v)-> e.evalColumn(v, false, false);
		case FILTER: 		return RequestEntryChain::evalFilter;
		case ORDER: 		return RequestEntryChain::evalOrder;
		case QUERY: 		return RequestEntryChain::evalQuery;
		case JOIN:			return RequestEntryChain::evalJoin;
		case PARTITION:		return RequestEntryChain::evalPartition;
		default:			throw unsupportedTypeException(type);
		}
	}
		
	private static UnsupportedOperationException unsupportedTypeException(JavaType type) {
		return new UnsupportedOperationException("unsupported type " + type.toString());
	}
}
