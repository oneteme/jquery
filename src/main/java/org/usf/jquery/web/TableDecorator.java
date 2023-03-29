package org.usf.jquery.web;

import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.DBComparator.equal;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.RequestQuery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface TableDecorator extends DBTable {
	
	String identity(); //URL
	
	@Override
	default String reference() { //JSON & TAG == URL 
		return identity();
	}
	
	String columnName(ColumnDecorator desc);
	
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		
		var query = new RequestQuery().select(this);
		parseColumns(ant, query, parameterMap);
		parseFilters(ant, query, parameterMap);
		return query;
	}
	
	default void parseColumns(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameterMap) {

		var cols = parameterMap.getOrDefault(ant.columnParameter(), ant.defaultColumns());
		if(isEmpty(cols)) { //TD check first param isBlank
			throw new IllegalArgumentException("require " + ant.columnParameter() + " parameter");
		}
		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
		flatStream(cols).forEach(p->{
			var res = parseResource(p, map, false);
			query.tables(res.getTableDecorator());
			query.columns(res.getColumnDecorator().column(res.getTableDecorator()));
		});
	}

	default void parseFilters(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
    	parameterMap.entrySet().stream()
    	.filter(e-> !ant.columnParameter().equals(e.getKey()) && !ant.revisionParameter().equals(e.getKey()))
    	.forEach(e->{
			var left = parseResource(e.getKey(), map, ant.allowUnknownParameters());
			if(left != null) {
				var column = left.getColumnDecorator().column(left.getTableDecorator());
				var values = flatStream(e.getValue()).collect(toList());
				var variables = values.stream().filter(o-> o.startsWith("$")).collect(toList());
				if(!variables.isEmpty()) {
					for(var v : variables) {
 						var right = parseResource(v.substring(1), map, false);
 						var exprs = equal(right.getColumnDecorator().column(right.getTableDecorator()));
 	 					query.tables(right.getTableDecorator());
 	 					query.filters(column.filter(exprs));
					}
					values.removeIf(o-> o.startsWith("$"));
				}
				if(!values.isEmpty()) {
 					var meta = DatabaseScanner.get().metadata().table(left.getTableDecorator()); 
					var expres = left.getColumnDecorator().expression(meta, values.toArray(String[]::new));
 					query.filters(column.filter(expres));
				}
			}
    	});
	}
	
	default void parseFilters() {
		
	}
	
	default Resource parseResource(String value, Map<String, ColumnDecorator> map, boolean ignoreUnkown) {
		TableDecorator table;
		String cn;
		String fn = null;
		var arr = value.split("\\.");
		if(arr.length == 1) {//column
			table = this;
			cn = arr[0];
		}
		else if(arr.length > 1 && arr.length < 4) {
			var tn  = formatColumnName(arr[0]);
			var res = DatabaseScanner.get().tables.stream().filter(t-> t.identity().equals(tn)).findAny();
			if(res.isPresent()) { //table.column
				table = res.get();
				cn = arr[1];
				if(arr.length > 2) { //table.column.fn
					fn = arr[2].toLowerCase();
				}
			}
			else { //column.expres
				table = this;
				cn = arr[0];
				fn = arr[1].toLowerCase();
				if(arr.length > 2) {
					throw new IllegalArgumentException("invalid resource " + value);
				}
			}
		}
		else {
			throw new IllegalArgumentException("invalid resource " + value);
		}
		var column = map.get(formatColumnName(cn));
		if(column == null) {
			if(ignoreUnkown) {
				return null;
			}
			throw new NoSuchElementException(cn + " not found");
		}
		return new Resource(table, column, fn);
	}
	
	// move to client side 
	default String formatColumnName(String v) {
		return v.replace("-", "_").toUpperCase();
	}
	
    static Stream<String> flatStream(String... arr) { //number local separator
    	return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
    }
    
    @Getter
    @RequiredArgsConstructor
    static class Resource {
    	private final TableDecorator tableDecorator;
    	private final ColumnDecorator columnDecorator;
    	private final String function;
    }
}
