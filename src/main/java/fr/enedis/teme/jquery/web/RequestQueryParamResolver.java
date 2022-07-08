package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Utils.toMap;
import static fr.enedis.teme.jquery.web.DatabaseScanner.get;
import static fr.enedis.teme.jquery.web.ParameterInvalidValueException.invalidParameterValueException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.columnNotFoundException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.tableNotFoundException;
import static fr.enedis.teme.jquery.web.TableMetadata.EMPTY_REVISION;
import static java.time.Month.DECEMBER;

import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
				? new PartitionedRequestQuery(parseRevisions(ant, table, parameterMap)) //must use partitions
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
	
	public static YearMonth[] parseRevisions(RequestQueryParam ant, DBTable table, Map<String, String[]> parameterMap) {

		var values = parameterMap.get(ant.revisionParameter());
		if(isEmpty(values)) {
			var currentRev = DatabaseScanner.get().metadata().latestRevision(table);
			return currentRev == null ? EMPTY_REVISION : new YearMonth[] { currentRev };
		}
		var revs = flatStream(values)
    			.map(RequestQueryParamResolver::parseYearMonth)
    			.toArray(YearMonth[]::new);
    	return DatabaseScanner.get().metadata().filterExistingRevision(table, ant.revisionMode(), revs);// can be null or empty
    }
		
	public static TaggableColumn[] parseColumns(RequestQueryParam ant, DBTable table, Map<String, String[]> parameterMap) {

		var cols = parameterMap.get(ant.columnParameter());
		var colStream = isEmpty(cols) //TD check first param isBlank
				? Stream.of(ant.defaultColumns())
				: flatStream(cols);
		var colMap = toMap(get().columnDescriptors(), ColumnDescriptor::key);
		return colStream.map(RequestQueryParamResolver::toEnumName).map(p->{
			try {
				return colMap.get(p).column(table);
			}catch (NullPointerException | IllegalArgumentException e) {
				throw columnNotFoundException(p);
			}
		}).toArray(TaggableColumn[]::new);
	}
	
	public static DBFilter[] parseFilters(RequestQueryParam ant, DBTable table, Map<String, String[]> parameterMap) {

		var colMap = toMap(get().columnDescriptors(), ColumnDescriptor::key);
    	var skipColumns = Set.of(ant.revisionParameter(), ant.columnParameter());
    	var filters = new LinkedList<DBFilter>();
		parameterMap.entrySet().stream().filter(e-> !skipColumns.contains(e.getKey())).forEach(p->{
 			var name = toEnumName(p.getKey());
 			var desc = colMap.get(name);
 			if(desc == null && !ant.allowUnknownParameters()) {
				throw columnNotFoundException(name);
 			}
 			if(desc != null) {
				var c = desc.column(table);
				if(c instanceof TableColumn) {
					var meta = DatabaseScanner.get().metadata().table(table).column((TableColumn)c);
					var filter = c.filter(desc.expression(meta, flatArray(p.getValue())));
					filters.add(filter);
				}
				else {
					throw new UnsupportedOperationException("applying filter on " + name);//
				}
 			}
 			//warn
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
