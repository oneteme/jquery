package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.RequestColumn.decode;
import static org.usf.jquery.web.RequestFilter.decode;
import static org.usf.jquery.web.RequestFilter.flatStream;

import java.util.Map;
import java.util.NoSuchElementException;

import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.NamedTable;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.RequestQuery;

/**
 * 
 * @author u$f
 *
 */
public interface TableDecorator {
	
	String identity(); //URL
	
	default String reference() { //JSON 
		return identity();
	}
	
	String tableName(); //
	
	String columnName(ColumnDecorator desc);
	
	default NamedTable table() {
		DBTable tab = b-> tableName();
		return tab.as(reference());
	}
	
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var query = new RequestQuery().select(table());
		parseColumns(ant, query, parameterMap);
		parseFilters(ant, query, parameterMap);
		parseOrders(ant, query, parameterMap);
		return query;
	}
	
	default void parseColumns(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		if(parameters.containsKey(COLUMN) && parameters.containsKey(COLUMN_DISTINCT)) {
			throw new IllegalArgumentException(); //message ?
		}
		var cols = parameters.containsKey(COLUMN) ? parameters.get(COLUMN) : parameters.get(COLUMN_DISTINCT);
		if(isEmpty(cols)) { //TD check first param isBlank
			throw new NoSuchElementException("require " + COLUMN + " parameter");
		}
		flatStream(cols).forEach(p->{
			var rc = decode(p, this, false);
			query.tables(rc.getTableDecorator().table()).columns(rc.column());
		});
	}

	default void parseFilters(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.filter(e-> RESERVED_WORDS.contains(e.getKey()))
    	.forEach(e-> {
    		var rf = decode(e, this); // catch exception => allowUnknownParameters
    		query.tables(rf.tables()).filters(rf.filters());
    	});
	}

	default void parseOrders(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		var cols = parameters.get("order");
		if(nonNull(cols)) {
			flatStream(cols).forEach(p->{
				var rc = decode(p, this, true);
				var col = rc.column();
				query.orders(isNull(rc.getExpression()) 
						? col.order() 
						: col.order(Order.valueOf(rc.getExpression())));
			});
		}
	}
}
