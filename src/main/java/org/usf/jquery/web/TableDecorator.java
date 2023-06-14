package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.RequestQuery;

/**
 * 
 * @author u$f
 *
 */
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
			throw new NoSuchElementException("require " + ant.columnParameter() + " parameter");
		}
		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
		Stream.of(cols).forEach(p->{
			var rc = RequestColumn.decode(p, this, map);
			query.tables(rc.getTableDecorator()).columns(rc.column());
		});
	}

	default void parseFilters(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDecorator::identity);
    	parameterMap.entrySet().stream()
    	.filter(e-> !ant.columnParameter().equals(e.getKey()) && !ant.revisionParameter().equals(e.getKey()))
    	.forEach(e->{
    		var rf = RequestFilter.decode(e, this, map);
    		query.tables(rf.tables()).filters(rf.fitlers());
    	});
	}
	
}
