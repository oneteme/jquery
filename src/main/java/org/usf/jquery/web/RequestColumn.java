package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.DBFunction.lookup;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.web.DatabaseScanner.database;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.TypedFunction;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * <code>[table.]column[.function]*[.comparator|order][:alias]</code>
 * 
 * @author u$f
 * 
 * @see RequestFilter
 * 
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestColumn {
	
	private final TableDecorator td;
	private final ColumnDecorator cd;
	private final List<TypedFunction> fns = new LinkedList<>();
	private final String exp;

	public TableDecorator tableDecorator() {
		return td;
	}
	
	public ColumnDecorator columnDecorator() {
		return fns.isEmpty() ? cd : wrap(cd, fns);
	}
	
	public String expression() {
		return exp;
	}

	private RequestColumn append(TypedFunction fn) {
		fns.add(fn);
		return this;
	}

	static RequestColumn decodeColumn(String value, TableDecorator defaultTable, boolean allowedExp) {
		return decodeColumn(value, defaultTable, 
				allowedExp ? v-> lookup(v).isEmpty() : v-> false); //not function
	}
	
	static RequestColumn decodeColumn(String value, TableDecorator defaultTable, Predicate<String> allowedExp) {
		var arr = value.split("\\.");
		var limit = arr.length > 1 && allowedExp.test(arr[arr.length-1]) ? arr.length-2 : arr.length-1;
		return decode(arr, limit, defaultTable);
	}
	
	private static RequestColumn decode(String[] arr, int limit, TableDecorator defaultTable) {
		var value = arr[limit]; //count | table.count
		if(database().isDeclaredColumn(value)) {//column found => break recursive call
			var cd = database().getColumn(value);
			if(limit > 1) {
				throw new IllegalArgumentException("too many prefix : " + join(".", copyOfRange(arr, 0, limit)));
			}
			var td = limit == 0 ? defaultTable : database().getTable(arr[0]);
			return new RequestColumn(td, cd, limit == arr.length-1 ? null : arr[arr.length-1]);
		}
		if(limit == 0) {
			throw unknownEntryException(value);
		}
		var fn = lookup(value).orElseThrow(()-> unknownEntryException(value));
		return decode(arr, --limit, defaultTable).append(fn);
	}
	
	private static ColumnDecorator wrap(final ColumnDecorator cd, final List<TypedFunction> fns) {
		
		return new ColumnDecorator() {
			
			@Override
			public String reference() {
				return cd.reference(); //add alias
			}
			
			@Override
			public String identity() {
				return cd.identity(); //add 
			}
			
			@Override
			public ColumnBuilder columnBuilder() { //logical column
				return t-> {
					DBColumn col = ColumnDecorator.super.column(t);
					for(var fn : fns) {//reduce ?
						col = fn.args(col);
					}
					return col.as(reference());
				};
			}
			
			@Override
			public int dataType() {
				var i = fns.size();
				while(--i>=0 && fns.get(i).getReturnedType() == AUTO_TYPE);
				return i<0 ? cd.dataType() : fns.get(i).getReturnedType();
			}
		};
	}
	
    private static IllegalArgumentException unknownEntryException(String v) {
    	return new IllegalArgumentException("unknown entry : " + v);
    }
}