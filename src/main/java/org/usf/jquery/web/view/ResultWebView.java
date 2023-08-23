package org.usf.jquery.web.view;

import static java.lang.Math.max;
import static java.lang.String.join;
import static java.time.ZoneId.systemDefault;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.web.view.ResultWebView.WebType.NUMBER;
import static org.usf.jquery.web.view.ResultWebView.WebType.STRING;
import static org.usf.jquery.web.view.ResultWebView.WebType.typeOf;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.usf.jquery.core.ResultMapper;
import org.usf.jquery.core.SqlStringBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 *
 */
public interface ResultWebView extends ResultMapper<Void>  {
	
	static Entry<String, WebType> requireNumberColumn(ResultSetMetaData rsm) throws SQLException {
    	var idx  = rsm.getColumnCount();
    	var yCol = entry(rsm.getColumnName(idx), typeOf(rsm.getColumnType(idx))); //y column
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
				.map(SqlStringBuilder::doubleQuote) //escape ' character
				.orElseGet(()-> doubleQuote("")));
		
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
			return name().toLowerCase();
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	  
    @Getter
    @RequiredArgsConstructor
    static final class DataTable {
    	
    	private final List<TableColumn> columns; //indexed
    	private final List<List<String>> rows; //indexed
    	
    	public void append(ResultSet rs) throws SQLException {
    		var arr = new ArrayList<String>(max(2, columns.size()));
    		for(var c : columns) {
    			arr.add(c.format(0, rs));
    		}
    		rows.add(arr);
    	}
    	
    	public static DataTable init(ResultSetMetaData rsm) throws SQLException {
    		var yAxis = new LinkedList<TableColumn>();
    		var xAxis = new LinkedList<TableColumn>();
    		for(var i=0; i<rsm.getColumnCount(); i++) {
    			var tc = new TableColumn(rsm.getColumnName(i+1), typeOf(rsm.getColumnType(i+1)));
    			(tc.getType() == NUMBER ? yAxis : xAxis).add(tc);
    		}
    		var arr = new ArrayList<>(yAxis);
    		if(xAxis.isEmpty()) {
    			arr.add(0, new TableColumn("", STRING, idx-> yAxis.stream().map(c-> c.getName()).collect(joining(",")))); //join yAxis
    		}
    		else if(xAxis.size() > 1) {
    			arr.add(0, new TableColumn(join(",", ""), STRING, idx-> xAxis.stream().map(c-> c.getName()).collect(joining(",")))); //join xAxis
    		}
    		else {
    			arr.add(0, xAxis.get(0));
    		}
    		return new DataTable(arr, new LinkedList<>());
    	}
    }
    
    @Getter
    @RequiredArgsConstructor
    static final class TableColumn {

    	private final String name;
    	private final WebType type;
    	private final IntFunction<Object> supplier;
    	
    	public TableColumn(String name, WebType type) {
    		this.name = name;
    		this.type = type;
    		this.supplier = null;
    	}
    	
		public String format(int index, ResultSet rs) throws SQLException {
			return type.format(supplier == null ? rs.getObject(name) : supplier.apply(index));
		}
    }
	
}
