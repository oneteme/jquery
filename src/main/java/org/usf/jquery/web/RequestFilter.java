package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.DBComparator.equal;
import static org.usf.jquery.core.DBComparator.greaterOrEqual;
import static org.usf.jquery.core.DBComparator.greaterThan;
import static org.usf.jquery.core.DBComparator.in;
import static org.usf.jquery.core.DBComparator.lessOrEqual;
import static org.usf.jquery.core.DBComparator.lessThan;
import static org.usf.jquery.core.DBComparator.like;
import static org.usf.jquery.core.DBComparator.iLike;
import static org.usf.jquery.core.DBComparator.notEqual;
import static org.usf.jquery.core.DBComparator.notIn;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBTable;

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
public class RequestFilter {
	
	private static final Set<String> symbols = Set.of("gt", "ge", "lt", "le", "not", "like", "ilike");
	
	private final RequestColumn requestColumn;
	private final List<RequestColumn> rightColumns;
	private final List<String> rightConstants;
	private final String comparator;
	
	public DBFilter[] fitlers() {
		var cmp = isNull(comparator) ? equal() : comparator(comparator);
		var column = requestColumn.column();
		var filters = rightColumns.stream().map(c-> column.filter(cmp.expression(c.column()))).collect(toList());
		if(isNull(comparator) && requestColumn.getFunctions().isEmpty()) { //no function & no comparator
			var meta = requestColumn.tableMetadata();
			filters.add(column.filter(requestColumn.getColumnDecorator().expression(meta, rightConstants.toArray(String[]::new))));
		}
		else {
			var parser = ColumnMetadata.parser(requestColumn.returnedType(), requestColumn.getColumnDecorator().reference());
			if(rightConstants.size() > 1 && (isNull(comparator) || "not".equals(comparator))) {
				var inCmp = isNull(comparator) ? in() : notIn();
				filters.add(column.filter(inCmp.expression(parser.parseArgs(rightConstants.toArray(String[]::new)))));
			}
			else {
				rightConstants.stream().map(v-> column.filter(cmp.expression(parser.parseArg(v)))).forEach(filters::add); //parse
			}
		}
		return filters.toArray(DBFilter[]::new);
	}

	public DBTable[] tables() {
		Set<DBTable> tables = new LinkedHashSet<>();
		tables.add(requestColumn.getTableDecorator());
		rightColumns.forEach(c-> tables.add(c.getTableDecorator()));
		return tables.toArray(DBTable[]::new);
	}
	
	static RequestFilter decode(Entry<String, String[]> entry, TableDecorator defaultTable, Map<String, ColumnDecorator> map) {
		var arr = entry.getKey().split("\\.");
		var cmp = symbols.contains(arr[arr.length-1]); //check last entry
		var col = RequestColumn.decode(arr, cmp ? arr.length-2 : arr.length-1, defaultTable, map); //ignore comparator 
		var cols = new LinkedList<RequestColumn>();
		var vals = new LinkedList<String>();
		flatStream(entry.getValue()).forEach(v->{
			if(v.startsWith("$")) { //extract variables
				cols.add(RequestColumn.decode(v.substring(1), defaultTable, map));
			}
			else {
				vals.add(v);
			}
		});
		return new RequestFilter(col, cols, vals, cmp ? arr[arr.length-1] : null);
	}
	
	private static DBComparator comparator(String comparator) {
		switch(comparator) {
		case "gt" :	return greaterThan();
		case "ge" : return greaterOrEqual();
		case "lt" : return lessThan();
		case "le" : return lessOrEqual();
		case "not": return notEqual();
		case "like": return (b, args)-> {
			args[1] = "%" + args[1] + "%"; //not works with columns
			return like().sql(b, args);
		};
		case "ilike": return (b, args)-> {
			args[1] = "%" + args[1] + "%"; //not works with columns
			return iLike().sql(b, args);
		};
		default: return null;
		}
	}

    static Stream<String> flatStream(String... arr) { //number local separator
    	return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
    }
}
