package org.usf.jquery.web;

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.Order;
import org.usf.jquery.core.OrderColumn;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RequestOrder {

	private static final Set<String> symbols = Stream.of(Order.values()).map(Order::name).map(String::toLowerCase).collect(toSet());
	
	private final RequestColumn requestColumn;
	private final String order;

	
	public OrderColumn order() {
		return new OrderColumn(requestColumn.column(), Order.valueOf(order));
	}
	
	static RequestOrder decode(Entry<String, String[]> entry, TableDecorator defaultTable, Map<String, ColumnDecorator> map) {

		var arr = entry.getKey().split("\\.");
		var ord = symbols.contains(arr[arr.length-1]); //check last entry
		var col = RequestColumn.decode(arr, ord ? arr.length-2 : arr.length-1, defaultTable, map); //ignore comparator 
		return new RequestOrder(col, ord ? arr[arr.length-1] : null);
	}

}
