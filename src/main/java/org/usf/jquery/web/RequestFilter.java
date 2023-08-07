package org.usf.jquery.web;

import static org.usf.jquery.web.RequestColumn.decodeColumn;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.BasicComparator;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBTable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * RequestColumn=val1,val2,RequestColumn2,...
 * 
 * @author u$f
 * 
 * @see RequestColumn
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestFilter {
	
	private final RequestColumn requestColumn;
	private final List<RequestColumn> rightColumns;
	private final List<String[]> rightValues;

	public DBTable[] tables() {
		Set<DBTable> tables = new LinkedHashSet<>();
		tables.add(requestColumn.tableDecorator().table());
		rightColumns.forEach(c-> tables.add(c.tableDecorator().table()));
		return tables.toArray(DBTable[]::new);
	}
	
	public DBFilter[] filters() { // do not join filters (WHERE + HAVING)
		var td  = requestColumn.tableDecorator();
		var cd  = requestColumn.columnDecorator();
		var col = td.column(cd);
		var filters = new LinkedList<>();
		if(!rightColumns.isEmpty()) {
			var cmp = cd.comparator(requestColumn.expression(), 1);
			if(cmp instanceof BasicComparator) {
				rightColumns.stream()
				.map(c-> col.filter(cmp.expression(c.tableDecorator().column(c.columnDecorator()))))
				.forEach(filters::add);
			}
			else {
				throw new IllegalArgumentException("illegal column comparator " + requestColumn.expression());
			}
		}
		if(!rightValues.isEmpty()) {
			rightValues.forEach(arr->
				filters.add(col.filter(td.expression(cd, 
						requestColumn.expression(), arr))));
		}
		return filters.toArray(DBFilter[]::new);
	}
	
	static RequestFilter decodeFilter(Entry<String, String[]> entry, TableDecorator defaultTable) {
		var col  = decodeColumn(entry.getKey(), defaultTable, true); //allow comparator
		var cols = new LinkedList<RequestColumn>();
		var vals = new LinkedList<String[]>();
		Stream.of(entry.getValue()).forEach(v->{
			if(v.contains(",")) { //multiple values
				vals.add(v.split(","));
			}
			else if(v.matches("^[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)+$")) { //table.column[.function]*
				try {
					cols.add(decodeColumn(v, defaultTable, false)); //deny expression
				}
				catch (Exception e) {
					vals.add(new String[] {v});
				}
			}
			else {
				vals.add(new String[] {v});
			}
		});
		return new RequestFilter(col, cols, vals);
	}
}
