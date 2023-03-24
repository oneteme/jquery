package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toMap;
import static org.usf.jquery.web.ResourceNotFoundException.columnNotFoundException;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.RequestQuery;
import org.usf.jquery.core.TaggableColumn;

public interface TableDecorator extends DBTable {
	
	String identity(); //URL

	@Override
	String reference(); //SQL
	
	String columnReference(ColumnDecorator desc);
	
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var meta = DatabaseScanner.get().metadata().table(this);
		return new RequestQuery().select(this)
				.columns(ant.columns(), ()-> parseColumns(ant, meta, parameterMap))
				.filters(ant.filters(), ()-> parseFilters(ant, meta, parameterMap));
	}
		
	default TaggableColumn[] parseColumns(RequestQueryParam ant, TableMetadata metadata, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
		var cols = parameterMap.get(ant.columnParameter());
		if(isEmpty(cols)) {
			cols = ant.defaultColumns();
		}
		if(isEmpty(cols)) { //TD check first param isBlank
			throw new IllegalArgumentException("require " + ant.columnParameter() + " parameter");
		}
		return flatStream(cols).map(p->{
			var dcr = map.get(formatColumnName(p));
			if(dcr == null) {
				throw columnNotFoundException(p);
			}
			return dcr.column(this);
		}).toArray(TaggableColumn[]::new);
	}
	
	default DBFilter[] parseFilters(RequestQueryParam ant, TableMetadata metadata, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
    	var skipColumns = Set.of(ant.revisionParameter(), ant.columnParameter());
    	var filters = new LinkedList<DBFilter>();
    	for(var e : parameterMap.entrySet()) {
 			if(!skipColumns.contains(e.getKey())) {
 	    		var name = formatColumnName(e.getKey());
 				var dcr = map.get(name);
 				if(dcr != null) {
 					filters.add(dcr.filter(this, metadata, flatArray(e.getValue())));
 				}
 	 			else if(!ant.allowUnknownParameters()) {
 					throw columnNotFoundException(e.getKey());
 	 			}
 			}
    	}
		return filters.toArray(DBFilter[]::new);
	}

	@Deprecated(forRemoval = true) // move to client side 
	static String formatColumnName(String v) {
		return v.replace("-", "_").toUpperCase();
	}
	
    static String[] flatArray(String... arr) {
    	return flatStream(arr).toArray(String[]::new);
    }

    static Stream<String> flatStream(String... arr) { //number local separator
    	return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
    }
}
