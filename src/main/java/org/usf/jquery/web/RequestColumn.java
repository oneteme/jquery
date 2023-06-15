package org.usf.jquery.web;

import static java.lang.String.join;
import static java.sql.Types.JAVA_OBJECT;
import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.DBFunction.lookup;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedFunction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestColumn {
	
	private final TableDecorator tableDecorator;
	private final ColumnDecorator columnDecorator;
	private final List<TypedFunction> functions = new LinkedList<>();
	
	public RequestColumn append(TypedFunction fn) {
		functions.add(fn);
		return this;
	}
	
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

	public TableMetadata tableMetadata() {
		return DatabaseScanner.get().metadata().table(tableDecorator);
	}
	
	public ColumnMetadata columnMetadata() {
		return tableMetadata().column(columnDecorator);
	}
	
	public int returnedType() {
		var i = functions.size();
		while(--i>=0) {
			var type = functions.get(i).getReturnedType();
			if(type != JAVA_OBJECT) {
				return type;
			}
		}
		return DatabaseScanner.get().metadata().table(tableDecorator).getColumns().get(columnDecorator.identity()).getType();
	}
	
	static RequestColumn decode(String value, TableDecorator defaultTable, Map<String, ColumnDecorator> map) {
		var arr = value.split("\\.");
		return decode(arr, arr.length-1, defaultTable, map);
	}
	
	static RequestColumn decode(String[] arr, int limit, TableDecorator defaultTable, Map<String, ColumnDecorator> map) {
		var value = arr[limit];
		if(map.containsKey(value)) {//column found => break recursive call
			var cd = map.get(value);
			TableDecorator td;
			if(limit > 1) {
				throw new IllegalArgumentException("too many prefix : " + join(".", copyOfRange(arr, 0, limit)));
			}
			td = limit == 0 ? defaultTable : DatabaseScanner.get().tables.stream()
					.filter(t-> t.identity().equals(arr[0]))
					.findFirst()
					.orElseThrow(()-> unknownEntryException(arr[0]));
			return new RequestColumn(td, cd);
		}
		if(limit == 0) {
			throw unknownEntryException(value);
		}
		var fn = lookup(value).orElseThrow(()-> unknownEntryException(value));
		return decode(arr, --limit, defaultTable, map).append(fn);
	}
	
    private static IllegalArgumentException unknownEntryException(String v) {
    	return new IllegalArgumentException("unknown entry : " + v);
    }
}