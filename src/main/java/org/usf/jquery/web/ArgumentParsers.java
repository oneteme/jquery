package org.usf.jquery.web;

import static java.util.Collections.addAll;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JQueryType.COLUMN;
import static org.usf.jquery.core.JQueryType.QUERY_COLUMN;
import static org.usf.jquery.core.Utils.isEmpty;
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
import org.usf.jquery.core.QueryContext;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
			TIME, TIMESTAMP_WITH_TIMEZONE, VARCHAR }; //varchar !?

	public static Object parse(RequestEntryChain entry, ViewDecorator td, QueryContext ctx, JavaType... types) {
		List<JavaType> list = new ArrayList<>();
		if(isEmpty(types) || Stream.of(types).anyMatch(JDBCType.class::isInstance)) {
			list.add(COLUMN);
			list.add(QUERY_COLUMN);
		}
		addAll(list, isEmpty(types) ? STD_TYPES : types);
		var exList = new ArrayList<Exception>();
		for(var type : list) {
			try {
				if(type instanceof JDBCType t) {
					return jdbcArgParser(t).parseEntry(entry, td);
				}
				if(type instanceof JQueryType t) {
					return jqueryArgParser(t, ctx).parseEntry(entry, td);
				}
				else {
					throw new UnsupportedOperationException(requireNonNull(type, "type is null").toString());
				}
			} catch (NoSuchResourceException | EntryParseException e) { //do not throw exception
				exList.add(e);
			}
		}
		if(exList.size() > 1) {
			for(int i=0; i<exList.size(); i++) {
				log.warn("parsing '{}' as {} => {}", entry, list.get(i), exList.get(i).getMessage());
			}
		}
		throw cannotParseEntryException("entry", entry, exList.size() == 1 ? exList.get(0) : null);
	}
	
	public static JDBCArgumentParser jdbcArgParser(JDBCType type) {
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

	public static JavaArgumentParser jqueryArgParser(JQueryType type, QueryContext ctx) {
		switch (type) {
		case QUERY_COLUMN:	return (e,v)-> e.evalQueryColumn(v, ctx);
		case NAMED_COLUMN:	return (e,v)-> e.evalColumn(v, ctx, true); //separate query context 
		case COLUMN:		return (e,v)-> e.evalColumn(v, ctx, false);
		case FILTER: 		return (e,v)-> e.evalFilter(v, ctx);
		case ORDER: 		return (e,v)-> e.evalOrder(v, ctx);
		case QUERY: 		return (e,v)-> e.evalQuery(v, ctx);
		case JOIN:			return (e,v)-> e.evalJoin(v, ctx);
		case PARTITION:		return (e,v)-> e.evalPartition(v, ctx);
		default:			throw unsupportedTypeException(type);
		}
	}
		
	private static UnsupportedOperationException unsupportedTypeException(JavaType type) {
		return new UnsupportedOperationException("unsupported type " + type.toString());
	}
}
