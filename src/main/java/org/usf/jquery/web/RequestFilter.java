package org.usf.jquery.web;

import static org.usf.jquery.web.RequestColumn.decodeSingleColumn;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBTable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * <code>RequestColumn=val1[,val2]*</code>
 * <code>RequestColumn=AnOtherRequestColumn</code>
 * 
 * @author u$f
 * 
 * @see RequestColumn
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestFilter {
	
	private final RequestColumn rc;
	private final List<String[]> constants;
	private final List<RequestColumn> columns;

	public DBTable[] tables() {
		Set<DBTable> tables = new LinkedHashSet<>();
		tables.add(rc.tableDecorator().table());
		columns.forEach(c-> tables.add(c.tableDecorator().table()));
		return tables.toArray(DBTable[]::new);
	}
	
	public DBFilter[] filters() {
		var col = rc.toColumn();
		var filters = new LinkedList<>();
		if(!columns.isEmpty()) {
			rc.expression(columns).map(col::filter).forEach(filters::add);
		}
		if(!constants.isEmpty()) {
			constants.stream().map(rc::expression).map(col::filter).forEach(filters::add);
		}
		return filters.toArray(DBFilter[]::new);// do not join filters (WHERE + HAVING)
	}
	
	static RequestFilter decodeFilter(Entry<String, String[]> entry, TableDecorator defaultTable) {
		var col = decodeSingleColumn(entry.getKey(), defaultTable, true); //allow comparator
		var columns = new LinkedList<RequestColumn>();
		var constants = new LinkedList<String[]>();
		Stream.of(entry.getValue()).forEach(v->{
			try {//TODO pattern
				columns.add(decodeSingleColumn(v, defaultTable, false)); //deny expression
			}
			catch (Exception e) { //TODO 
				constants.add(v.contains(",") ? v.split(",") : new String[] {v}); //check values
			}
		});
		return new RequestFilter(col, constants, columns);
	}
}
