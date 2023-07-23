package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.DBFunction.lookup;
import static org.usf.jquery.core.Utils.AUTO_TYPE;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TaggableTable;
import org.usf.jquery.core.TypedFunction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 * [table.]column[.function]*[.comparator|order]
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestColumn {
	
	private final TableDecorator tableDecorator;
	private final ColumnDecorator columnDecorator;
	private final List<TypedFunction> functions = new LinkedList<>();
	private final String expression;
	
	public TaggableColumn column() {
		DBColumn col = columnDecorator.column(tableDecorator);
		if(functions.isEmpty()) {
			return (TaggableColumn) col;
		}
		for(var fn : functions) {//reduce ?
			col = fn.args(col);
		}
		return col.as(columnDecorator.reference()); // not sure
	}

	public TaggableTable table() {
		return tableDecorator.table();
	}
		
	public int returnedType() {
		var i = functions.size();
		while(--i>=0) {
			var type = functions.get(i).getReturnedType();
			if(type != AUTO_TYPE) {
				return type;
			}
		}
		return columnDecorator.dbType();
	}

	private RequestColumn append(TypedFunction fn) {
		functions.add(fn);
		return this;
	}

	static RequestColumn decodeColumn(String value, TableDecorator defaultTable, boolean allowedExp) {
		return decode(value, defaultTable, 
				allowedExp ? v-> lookup(v).isEmpty() : v-> false); //not function
	}
	
	static RequestColumn decode(String value, TableDecorator defaultTable, Predicate<String> allowedExp) {
		var arr = value.split("\\.");
		var limit = arr.length > 1 && allowedExp.test(arr[arr.length-1]) ? arr.length-2 : arr.length-1;
		return decode(arr, limit, defaultTable);
	}
	
	static RequestColumn decode(String[] arr, int limit, TableDecorator defaultTable) {
		var value = arr[limit]; //count | table.count
		if(DatabaseScanner.get().columnMap.containsKey(value)) {//column found => break recursive call
			var cd = DatabaseScanner.get().columnMap.get(value);
			if(limit > 1) {
				throw new IllegalArgumentException("too many prefix : " + join(".", copyOfRange(arr, 0, limit)));
			}
			var td = limit == 0 ? defaultTable : DatabaseScanner.get().tables.stream()
					.filter(t-> t.identity().equals(arr[0]))
					.findAny()
					.orElseThrow(()-> unknownEntryException(arr[0]));
			return new RequestColumn(td, cd, limit == arr.length-1 ? null : arr[arr.length-1]);
		}
		if(limit == 0) {
			throw unknownEntryException(value);
		}
		var fn = lookup(value).orElseThrow(()-> unknownEntryException(value));
		return decode(arr, --limit, defaultTable).append(fn);
	}
	
    private static IllegalArgumentException unknownEntryException(String v) {
    	return new IllegalArgumentException("unknown entry : " + v);
    }
}