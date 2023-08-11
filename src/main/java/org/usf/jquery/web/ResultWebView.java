package org.usf.jquery.web;

import static java.time.ZoneId.systemDefault;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.web.ResultWebView.WebType.NUMBER;
import static org.usf.jquery.web.ResultWebView.WebType.STRING;
import static org.usf.jquery.web.ResultWebView.WebType.typeOf;

import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.usf.jquery.core.ResultMapper;
import org.usf.jquery.core.SqlStringBuilder;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
public interface ResultWebView extends ResultMapper<Void>  {
	
	static Entry<String, WebType> requireNumberColumn(ResultSetMetaData rsm) throws SQLException {
    	var last = rsm.getColumnCount();
    	var yCol = entry(rsm.getColumnName(last), typeOf(rsm.getColumnType(last))); //y column
		return yCol.getValue() == NUMBER ? yCol : requireColumn(rsm, NUMBER::equals);
	}
	
	static Entry<String, WebType> requireDateColumn(ResultSetMetaData rsm) throws SQLException {
    	var yCol = entry(rsm.getColumnName(1), typeOf(rsm.getColumnType(1)));  //x column
		return yCol.getValue().isDate() ? yCol : requireColumn(rsm, WebType::isDate);
	}
	
	static Entry<String, WebType> requireColumn(ResultSetMetaData rsm, Predicate<WebType> type) throws SQLException {
		var cols = columns(rsm).entrySet().stream()
				.filter(e-> type.test(e.getValue()))
				.collect(toList());
		if(cols.size() == 1) {
			return  cols.get(0);
		}
		throw new IllegalArgumentException("require one ?? column"); //TODO
	}

    static Map<String, WebType> columns(ResultSetMetaData rsm) throws SQLException {
    	var map = new LinkedHashMap<String, WebType>();
		for(var i=0; i<rsm.getColumnCount(); i++) {
			map.put(rsm.getColumnName(i+1), typeOf(rsm.getColumnType(i+1)));
		}
		return map;
    }
	
    static List<String> columns(ResultSetMetaData rsm, Predicate<String> test) throws SQLException {
    	List<String> columns = new LinkedList<>();
		for(var i=0; i<rsm.getColumnCount(); i++) {
			var cn = rsm.getColumnName(i+1);
			if(test.test(cn)) {
				columns.add(cn);
			}
		}
		return columns;
	}
	
	@RequiredArgsConstructor
	enum WebType implements Formatter<Object> {

		BOOLEAN(o-> ofNullable(o)
				.map(Object::toString)
				.orElse(null)),
		
		NUMBER(o-> ofNullable(o)
				.map(Object::toString)
				.orElse(null)),

		DATETIME(o-> ofNullable((Timestamp) o)
				.map(Timestamp::toInstant)
				.map(t-> "new Date('" + t + "')") //ISO
				.orElse(null)),
		
		DATE(o-> ofNullable((Date) o)
				.map(Date::toLocalDate)
				.map(d-> d.atStartOfDay().atZone(systemDefault()).toInstant())
				.map(t-> "new Date('" + t + "')") //ISO
				.orElse(null)), 
		
		STRING(o-> ofNullable(o)
				.map(Object::toString)
				.map(SqlStringBuilder::quote)
				.orElse(null));
		
		//TD: google types timeOfDate, Date
		
		private final Formatter<Object> formatter;
		
		@Override
		public String format(Object o) {
			return formatter.format(o);
		}
		
		public boolean isDate() {
			return this == DATE || this == DATETIME;
		}
		
		public String typeName() {
			return (isDate() ? DATE : this).name().toLowerCase();
		}
		
		static WebType typeOf(int type) {
			switch (type) {
			case Types.BOOLEAN: return BOOLEAN;
			case Types.BIT:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.REAL:
			case Types.FLOAT:
			case Types.DOUBLE:
			case Types.NUMERIC:
			case Types.DECIMAL: return NUMBER;
			case Types.DATE: return DATE;
			case Types.TIMESTAMP: return DATETIME;
			//case Types.TIME: //need explicit cast format !?
			default: return STRING;
			}
		}
	}
	
	@FunctionalInterface
	interface Formatter<T> {
		
		String format(T o);
		
		public static <T> Formatter<Collection<T>> formatCollection(String delimiter) {
			return c-> STRING.format(c.stream().map(Object::toString).collect(joining(delimiter)));
		}
	}
}
