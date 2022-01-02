package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.reflect.DatabaseScanner.metadata;
import static fr.enedis.teme.jquery.web.InvalidParameterValueException.invalidParameterValueException;
import static fr.enedis.teme.jquery.web.MissingParameterException.missingParameterException;
import static fr.enedis.teme.jquery.web.RequestQueryParam.Mode.INCLUDE;
import static fr.enedis.teme.jquery.web.ResourceAccessDeniedException.tableAccessDeniedException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.columnNotFoundException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.tableNotFoundException;
import static java.time.Month.DECEMBER;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import fr.enedis.teme.jquery.DBFilter;
import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.PartitionedRequestQuery;
import fr.enedis.teme.jquery.RequestQuery;
import fr.enedis.teme.jquery.TableColumn;
import fr.enedis.teme.jquery.YearPartitionTable;
import fr.enedis.teme.jquery.web.RequestQueryParam.Mode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQuery requestQuery(RequestQueryParam ant, Map<String, String[]> parameterMap, Supplier<String> requestURIFn) {
		
		var table = parseTable(ant.names(), ant.mode(), ant.value(), requestURIFn);
		RequestQuery rq = YearPartitionTable.class.isAssignableFrom(ant.value())
				? new PartitionedRequestQuery(parseRevision(ant.revisionParameter(), parameterMap, table)) //must use partitions
				: new RequestQuery();
		return rq.select(table)
				.columns(ant.columns(), ()-> parseColumns(ant.columnParameter(), table.columns(), parameterMap))
				.filters(ant.filters(), ()-> parseFilters(table.columns(), parameterMap, table));
	}
	
	public static YearMonth[] parseRevision(String parameterName, Map<String, String[]> parameterMap, DBTable table) {

		var rev = parameterMap.get(parameterName);
		if(rev == null || rev.length == 0) {
			throw missingParameterException(parameterName);
		}
    	return Stream.of(flatArray(rev))
    			.map(RequestQueryParamResolver::parseYearMonth)
    			.map(r-> metadata().requireRevision(table, r))
    			.toArray(YearMonth[]::new);
    }
	
	public static DBTable parseTable(String[] names, Mode mode, Class<? extends Enum<? extends DBTable>> enumType, Supplier<String> requestURIFn) {
		
		String tableName;
		if(names.length == 1 && mode == INCLUDE) {
			tableName = names[0];
		}
		else {
			var arr = requestURIFn.get().split("/"); //TD optim use regex
			tableName = toEnumName(arr[arr.length-1]);
		}
		var table = (DBTable) Stream.of(enumType.getEnumConstants()) //enumOf JAVA not helping
				.filter(e-> e.name().equals(tableName))
				.findAny().orElseThrow(()-> tableNotFoundException(tableName));
		if(names.length != 0) {
			var found = Stream.of(names).anyMatch(tableName::equals);
			if(found ^ mode == INCLUDE) {
				throw tableAccessDeniedException(tableName);
			}
		}
		return table;
	}
	
	public static TableColumn[] parseColumns(String parameterName, TableColumn[] columns, Map<String, String[]> parameterMap) {

		var cols = parameterMap.get(parameterName);
		if(cols == null || cols.length == 0) {
			return columns;
		}
		var colMap = Stream.of(columns).collect(toMap(TableColumn::name, identity()));
		return flatStream(cols)
			.map(p->{
				var column = colMap.get(toEnumName(p));
				if(column == null) {
					throw columnNotFoundException(p);
				}
				return column;
			}).toArray(TableColumn[]::new);
	}
	
	public static DBFilter[] parseFilters(TableColumn[] columns, Map<String, String[]> parameterMap, DBTable table) {
		
		var colMap = Stream.of(columns).collect(toMap(TableColumn::name, identity()));
		var filters = new LinkedList<DBFilter>();
		parameterMap.entrySet().forEach(p->{
 			var name = toEnumName(p.getKey());
 			 ofNullable(colMap.get(name)).ifPresent(c->{
 				var values = flatArray(p.getValue()); //check types before
				filters.add(values.length == 1 
						? c.equalFilter(metadata().typedValue(table, c, values[0])) 
						: c.inFilter(metadata().typedValues(table, c, values)));
 			});
		});
		return filters.toArray(DBFilter[]::new);
	}
	
    private static YearMonth parseYearMonth(String rev) {
    	if(rev.matches("[0-9]{4}-[0-9]{2}")) {
    		return YearMonth.parse(rev);
    	}
    	if(rev.matches("[0-9]{4}")) {
    		return Year.parse(rev).atMonth(DECEMBER);
    	}
    	throw invalidParameterValueException(rev);
    }
	
	private static String toEnumName(String v) {
		return v.replace("-", "_").toUpperCase();
	}

    private static String[] flatArray(String... arr) {
    	return flatStream(arr).toArray(String[]::new);
    }

    private static Stream<String> flatStream(String... arr) { //number local separator
    	return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
    }
}
