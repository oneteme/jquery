package org.usf.jquery.web.view;

import static java.lang.String.join;
import static java.lang.String.valueOf;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.web.view.WebViewMapper.WebType.NUMBER;
import static org.usf.jquery.web.view.WebViewMapper.WebType.STRING;
import static org.usf.jquery.web.view.WebViewMapper.WebType.typeOf;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.usf.jquery.core.ResultSetMapper;
import org.usf.jquery.core.SqlStringBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author u$f
 *
 *
 */
public interface WebViewMapper extends ResultSetMapper<Void>  {
	
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
			return switch (type) {
			case Types.BOOLEAN: yield BOOLEAN;
			case Types.BIT, Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.REAL, Types.FLOAT, Types.DOUBLE, Types.NUMERIC, Types.DECIMAL: yield NUMBER;
			case Types.DATE: yield DATE;
			case Types.TIMESTAMP: yield DATETIME;
			//case Types.TIME: //need explicit cast format !?
			default: yield STRING;
			};
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
    	
    	private final TableColumn xAxis;
    	private final List<TableColumn> yAxis;
    	private final List<List<Entry<String, String>>> rows = new LinkedList<>();
    	
    	public void fetchRow(ResultSet rs) throws SQLException {
    		var arr = new ArrayList<Entry<String, String>>(yAxis.size()+ 1);
			arr.add(xAxis.format(rs));
    		for(var c : yAxis) {
    			arr.add(c.format(rs));
    		}
    		rows.add(arr);
    	}
    	
    	public static DataTable fromMetaData(ResultSetMetaData rsm) throws SQLException {
    		var xAxis = new TableColumn(rsm.getColumnName(1), typeOf(rsm.getColumnType(1)));
    		var yAxis = new LinkedList<TableColumn>();
    		var dimen = new LinkedList<TableColumn>();
    		if(rsm.getColumnCount() == 1) {
    			if(xAxis.getType() == NUMBER) {
    				yAxis.add(new TableColumn(xAxis.getName(), xAxis.getType()));
    			}
        		xAxis.prefixed("x-"+xAxis.getName()); //avoid same yAxis name
    		}
    		else {
        		for(var i=1; i<rsm.getColumnCount(); i++) {//require xAxis first column
        			var tc = new TableColumn(rsm.getColumnName(i+1), typeOf(rsm.getColumnType(i+1)));
        			(tc.getType() == NUMBER ? yAxis : dimen).add(tc);
        		}
        		xAxis.prefixed();
    		}
    		if(yAxis.isEmpty()) {
    			throw new IllegalArgumentException("require number column");
    		}
    		if(yAxis.size() > 1 || dimen.isEmpty()) {
				yAxis.forEach(TableColumn::prefixed);
    		}
    		if(!dimen.isEmpty()) {
    			var over = dimen.stream().map(TableColumn::getName).collect(toList());
				yAxis.forEach(c-> c.setOver(over));
    		}
    		return new DataTable(xAxis, yAxis);
    	}
    }

    @Getter
    @ToString
    @RequiredArgsConstructor
    static class TableColumn {

    	private final String name;
    	private final WebType type;
        @Setter
    	private List<String> over = emptyList(); 
    	private String prefixed = "";
    	
		public Entry<String, String> format(ResultSet rs) throws SQLException {
			var arr = new LinkedList<String>();
			for(var s : over) {
				arr.add(valueOf(rs.getObject(s)));
			}
			if(!isBlank(prefixed)) { //optim this
				arr.add(0, prefixed);
			}
			return entry(join("_", arr), type.format(rs.getObject(name)));
		}

		public void prefixed() {
			prefixed(name);
		}
		
		public void prefixed(String v) {
			this.prefixed = v;
		}
		
		static TableColumn[] columns(ResultSetMetaData rsm) throws SQLException {
	    	var arr = new TableColumn[rsm.getColumnCount()];
	    	for(var i=0; i<rsm.getColumnCount(); i++) {
	    		arr[i] = new TableColumn(rsm.getColumnName(i+1), typeOf(rsm.getColumnType(i+1)));
			}
			return arr;
	    }

    }
}
