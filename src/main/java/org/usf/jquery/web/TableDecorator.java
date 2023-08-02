package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.Constants.WINDOW_ORDER;
import static org.usf.jquery.web.Constants.WINDOW_PARTITION;
import static org.usf.jquery.web.MissingParameterException.missingParameterException;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;
import static org.usf.jquery.web.RequestColumn.decodeColumn;
import static org.usf.jquery.web.RequestFilter.decodeFilter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.DBWindow;
import org.usf.jquery.core.RequestQuery;

/**
 * 
 * @author u$f
 *
 */
public interface TableDecorator {
	
	String identity(); //URL
	
	String tableName(); //SQL
	
	Optional<String> columnName(ColumnDecorator cd); //optional
	
	default int columnType(ColumnDecorator cd) {
		return AUTO_TYPE;
	}

	default int columnSize(ColumnDecorator cd) {
		return UNLIMITED;
	}
	
	default DBTable table() {
		return new DBTable(tableName(), identity());
	}
	
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var query = new RequestQuery();
		parseWindow (ant, query, parameterMap);
		parseColumns(ant, query, parameterMap);
		parseFilters(ant, query, parameterMap);
		parseOrders (ant, query, parameterMap);
		return query;
	}
	
	default void parseWindow(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		var map = new LinkedHashMap<String, DBWindow>();
		if(parameters.containsKey(WINDOW_PARTITION)) {
			flatParameters(parameters.get(WINDOW_PARTITION)).forEach(p->{
				var rc = decodeColumn(p, this, false);
				var td = rc.tableDecorator();
				map.computeIfAbsent(td.identity(), k-> new DBWindow(td.table()))
				.partitions(rc.columnDecorator().column(td));
			});
		}
		if(parameters.containsKey(WINDOW_ORDER)) {
			flatParameters(parameters.get(WINDOW_ORDER)).forEach(p->{
				var rc = decodeColumn(p, this, true);
				var td = rc.tableDecorator();
				var col = rc.columnDecorator().column(td);
				map.computeIfAbsent(td.identity(), k-> new DBWindow(td.table()))
				.orders(isNull(rc.expression()) 
						? col.order() 
						: col.order(parseOrder(rc.expression())));
			});
		}
		if(map.isEmpty()) {
			query.tables(table());
		}
		else {
			map.values().forEach(w-> query.tables(w).filters(w.filter()));
		}
	}
	
	default void parseColumns(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		var cols = parameters.containsKey(COLUMN_DISTINCT) 
				? parameters.get(COLUMN_DISTINCT) 
				: parameters.get(COLUMN); //can be combined in PG (distinct on)
		if(isEmpty(cols)) {
			throw missingParameterException(COLUMN + "|" + COLUMN_DISTINCT);
		}
		if(parameters.containsKey(COLUMN_DISTINCT)){
			query.distinct();
		}//TD check first param !isBlank
		flatParameters(cols).forEach(p->{
			var rc = decodeColumn(p, this, false);
			var td = rc.tableDecorator();
			query.tablesIfAbsent(td.table()).columns(rc.columnDecorator().column(td));
		});
	}

	default void parseFilters(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		var ignoreParams = Set.of(ant.ignoreParameters()); 
    	parameters.entrySet().stream()
    	.filter(e-> !RESERVED_WORDS.contains(e.getKey()))
    	.filter(e-> !ignoreParams.contains(e.getKey()))
    	.forEach(e-> {
    		var rf = decodeFilter(e, this);
    		query.tablesIfAbsent(rf.tables()).filters(rf.filters());
    	});
	}

	default void parseOrders(RequestQueryParam ant, RequestQuery query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			flatParameters(parameters.get(ORDER)).forEach(p->{
				var rc = decodeColumn(p, this, true);
				var col = rc.columnDecorator().column(rc.tableDecorator());
				query.tablesIfAbsent(rc.tableDecorator().table())
				.orders(isNull(rc.expression()) 
						? col.order() 
						: col.order(parseOrder(rc.expression())));
			});
		}
	}
	
	static String parseOrder(String order) {
		if("desc".equals(order) || "asc".equals(order)) {
			return order.toUpperCase();
		}
		throw cannotEvaluateException(ORDER, order);
	}

	static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}
}
