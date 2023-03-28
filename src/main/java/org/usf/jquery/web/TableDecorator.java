package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.RequestQuery;

public interface TableDecorator extends DBTable {
	
	String identity(); //URL
	
	@Override
	default String reference() { //JSON & TAG == URL 
		return identity();
	}
	
	String columnName(ColumnDecorator desc);
	
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		return new RequestQuery().select(this)
				.columns(parseColumns(ant, parameterMap))
				.filters(parseFilters(ant, parameterMap));
	}
		
	default ColumnDecorator[] parseColumns(RequestQueryParam ant, Map<String, String[]> parameterMap) {

		var cols = parameterMap.getOrDefault(ant.columnParameter(), ant.defaultColumns());
		if(isEmpty(cols)) { //TD check first param isBlank
			throw new IllegalArgumentException("require " + ant.columnParameter() + " parameter");
		}
		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
		return flatStream(cols).map(p->{
			var column = map.get(formatColumnName(p));
			if(column == null) {
				throw new NoSuchElementException(p + " not found");
			}
			return column;
		}).toArray(ColumnDecorator[]::new);
	}
	
	default DBFilter[] parseFilters(RequestQueryParam ant, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
		var meta = DatabaseScanner.get().metadata().table(this);
    	var skipColumns = Set.of(ant.revisionParameter(), ant.columnParameter());
    	var filters = new LinkedList<DBFilter>();
    	for(var e : parameterMap.entrySet()) {
 			if(!skipColumns.contains(e.getKey())) {
 	    		var name = formatColumnName(e.getKey());
 				var dec = map.get(name);
 				if(dec != null) {
 					var column = dec.column(this);
 					var expres = dec.expression(meta, flatArray(e.getValue()));
 					filters.add(column.filter(expres));
 				}
 	 			else if(!ant.allowUnknownParameters()) {
 					throw new NoSuchElementException(e.getKey() + " not found");
 	 			}
 			}
    	}
		return filters.toArray(DBFilter[]::new);
	}

	// move to client side 
	default String formatColumnName(String v) {
		return v.replace("-", "_").toUpperCase();
	}
	
    static String[] flatArray(String... arr) {
    	return flatStream(arr).toArray(String[]::new);
    }

    static Stream<String> flatStream(String... arr) { //number local separator
    	return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
    }
}
