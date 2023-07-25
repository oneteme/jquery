package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.RequestColumn.decodeColumn;
import static org.usf.jquery.web.RequestFilter.decodeFilter;
import static org.usf.jquery.web.RequestFilter.flatStream;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
	
	String tableName(); //SQL
	
	String columnName(ColumnDecorator desc); 
	
	default int columnType(ColumnDecorator desc) {
		return AUTO_TYPE;
	}

	default int columnSize(ColumnDecorator desc) {
		return UNLIMITED;
	}
	
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
		var cols = parameters.containsKey(COLUMN_DISTINCT) 
				? parameters.get(COLUMN_DISTINCT) 
				: parameters.get(COLUMN);
		 //can be combined in PG (distinct on)
		if(isEmpty(cols)) {
			cols = ant.defaultColumns();
		}
		if(isEmpty(cols)) { //TD check first param isBlank
			throw new NoSuchElementException("require " + COLUMN + "|" + COLUMN_DISTINCT + " parameter");
		}
		flatStream(cols).forEach(p->{
			var rc = decodeColumn(p, this, false);
			query.tables(rc.table()).columns(rc.column());
		});
	}

	default void parseFilters(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		var ignoreParams = Set.of(ant.ignoreParameters()); 
    	parameters.entrySet().stream()
    	.filter(e-> !RESERVED_WORDS.contains(e.getKey()))
    	.filter(e-> !ignoreParams.contains(e.getKey()))
    	.forEach(e-> {
    		var rf = decodeFilter(e, this);
    		query.tables(rf.tables()).filters(rf.filters());
    	});
	}

	default void parseOrders(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		var cols = parameters.get(ORDER);
		if(nonNull(cols)) {
			flatStream(cols).forEach(p->{
				var rc = decodeColumn(p, this, true);
				var col = rc.column();
				query.orders(isNull(rc.getExpression()) 
						? col.order() 
						: col.order(Order.valueOf(rc.getExpression()))); //custom exception
			});
		}
	}
}
