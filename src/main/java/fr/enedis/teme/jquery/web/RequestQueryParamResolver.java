package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.Utils.isBlank;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.web.DatabaseScanner.metadata;
import static fr.enedis.teme.jquery.web.ParameterInvalidValueException.invalidParameterValueException;
import static fr.enedis.teme.jquery.web.ParameterRequiredException.missingParameterException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.columnNotFoundException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.tableNotFoundException;
import static java.time.Month.DECEMBER;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import fr.enedis.teme.jquery.ColumnTemplate;
import fr.enedis.teme.jquery.DBFilter;
import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.PartitionedRequestQuery;
import fr.enedis.teme.jquery.RequestQuery;
import fr.enedis.teme.jquery.TableColumn;
import fr.enedis.teme.jquery.TaggableColumn;
import fr.enedis.teme.jquery.YearPartitionTable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQuery requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		
		var table = parseTable(ant.name(), ant.value());
		RequestQuery rq = YearPartitionTable.class.isAssignableFrom(ant.value())
				? new PartitionedRequestQuery(parseRevision(ant, table, parameterMap)) //must use partitions
				: new RequestQuery();
		return rq.select(table)
				.columns(ant.columns(), ()-> parseColumns(ant, table, parameterMap))
				.filters(ant.filters(), ()-> parseFilters(ant, table, parameterMap));
	}
	
	public static DBTable parseTable(String tableName, Class<? extends Enum<? extends DBTable>> enumType) {
		
		return (DBTable) Stream.of(enumType.getEnumConstants()) //enumOf JAVA not helping
				.filter(e-> e.name().equals(tableName))
				.findAny()
				.orElseThrow(()-> tableNotFoundException(tableName));
	}
	
	public static YearMonth[] parseRevision(RequestQueryParam ant, DBTable table, Map<String, String[]> parameterMap) {

		var revs = parameterMap.get(ant.revisionParameter());
		if(isEmpty(revs)) {
			var currentRev = metadata().currentRevision(table);
			if(currentRev != null) {
				return new YearMonth[] { currentRev };
			}
			throw missingParameterException(ant.revisionParameter());
		}
    	return metadata().filterExistingRevision(table, 
    			Stream.of(flatArray(revs))
    			.map(RequestQueryParamResolver::parseYearMonth));
    	//TODO => can be empty
    }
		
	public static TaggableColumn[] parseColumns(RequestQueryParam ant, DBTable table, Map<String, String[]> parameterMap) {

		var cols = parameterMap.get(ant.columnParameter());
		var colStream = isEmpty(cols) || isBlank(cols[0]) 
				? Stream.of(ant.defaultColumns())
				: flatStream(cols);
		var colMap  = Stream.of(table.columns()).collect(toMap(TableColumn::name, identity()));
		Map<String, ColumnTemplate> tempMap = table.columnTemplates() == null 
				? emptyMap()
				: table.columnTemplates().stream().collect(toMap(ColumnTemplate::name, identity()));
		List<TaggableColumn> columns = new LinkedList<>();
		colStream
		.map(RequestQueryParamResolver::toEnumName)
		.forEach(p->{
			var c = colMap.get(p);
			var t = tempMap.get(p);
			if(t != null) {
				columns.addAll(t.getColumns());
			}
			else {
				if(c != null) {
					columns.add(c);
				}
				else{
					throw columnNotFoundException(p);
				}
			}
		});
		return columns.toArray(TaggableColumn[]::new);
	}
	
	public static DBFilter[] parseFilters(RequestQueryParam ant, DBTable table, Map<String, String[]> parameterMap) {
		
		var colMap = Stream.of(table.columns()).collect(toMap(TableColumn::name, identity()));
		var filters = new LinkedList<DBFilter>();
    	var knownColumns = Set.of(ant.revisionParameter(), ant.columnParameter());
		parameterMap.entrySet().stream().filter(e-> !knownColumns.contains(e.getKey())).forEach(p->{
 			var name = toEnumName(p.getKey());
 			var c = colMap.get(name);
 			if(c == null) {
				throw columnNotFoundException(name);
 			}
			var values = flatArray(p.getValue()); //check types before
			filters.add(values.length == 1 
					? c.equal(metadata().typedValue(table, c, values[0])) 
					: c.in(metadata().typedValues(table, c, values)));
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
