package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Arrays.copyOfRange;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.DBFunction.lookup;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.web.DatabaseScanner.database;

import java.util.LinkedList;
import java.util.List;

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
	private final String tag;

	public TableDecorator tableDecorator() {
		return td;
	}
	
	public ColumnDecorator columnDecorator() {
		return fns.isEmpty() ? cd : wrapColumn();
	}
	
	public String expression() {
		return exp;
	}

	private RequestColumn append(TypedFunction fn) {
		fns.add(fn);
		return this;
	}

	static RequestColumn decodeColumn(String value, TableDecorator defaultTable, boolean allowedExp) {
		
		if(!value.matches("^\\w+[\\.\\w+]*(\\:\\w+)?")) {
			throw new IllegalArgumentException("illegal column expression");
		}
		String tag = null;
		if(value.contains(":")) {
			var idx = value.lastIndexOf(':');
			tag = value.substring(idx+1);
			value = value.substring(0, idx);
		}
		var arr  = value.split("\\.");
		String exp = null;
		int from = arr.length-1;
		if(arr.length > 1 && allowedExp && lookup(arr[arr.length-1]).isEmpty()) {
			exp = arr[arr.length-1];
			from = arr.length-2;
		}
		return decode(arr, from, defaultTable, exp, tag);
	}
	
	private static RequestColumn decode(String[] arr, int index, TableDecorator defaultTable, String exp, String tag) {
		var value = arr[index]; //count | table.count
		if(database().isDeclaredColumn(value)) {//column found => break recursive call
			var cd = database().getColumn(value);
			if(index > 1) {
				throw new IllegalArgumentException("too many prefix : " + join(".", copyOfRange(arr, 0, index)));
			}
			var td = index == 0 ? defaultTable : database().getTable(arr[0]);
			return new RequestColumn(td, cd, exp, tag);
		}
		if(index == 0) {
			throw unknownEntryException(value);
		}
		var fn = lookup(value).orElseThrow(()-> unknownEntryException(value));
		return decode(arr, --index, defaultTable, exp, tag).append(fn);
	}
	
	private ColumnDecorator wrapColumn() {
		
		return new ColumnDecorator() {
			
			@Override
			public String reference() {
				return ofNullable(tag).orElseGet(cd::reference); 
			}
			
			@Override
			public String identity() {
				return cd.identity(); //unused
			}
			
			@Override
			public ColumnBuilder columnBuilder() { //logical column
				return t-> {
					DBColumn col = cd.column(t);
					return fns.stream()
							.reduce(col, (c, fn)-> fn.args(c), (c1,c2)-> c1) //Sequentially
							.as(reference());
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