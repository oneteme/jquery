package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Utils.toMap;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.columnNotFoundException;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import fr.enedis.teme.jquery.DBFilter;
import fr.enedis.teme.jquery.RequestQuery;
import fr.enedis.teme.jquery.TableColumn;
import fr.enedis.teme.jquery.TaggableColumn;

public interface TableDescriptor {
	
	String name(); //URL
	
	String value(); //SQL

	String columnName(ColumnDescriptor desc);
	
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var meta = DatabaseScanner.get().metadata().table(this);
		return new RequestQuery().select(value())
				.columns(ant.columns(), ()-> parseColumns(ant, meta, parameterMap))
				.filters(ant.filters(), ()-> parseFilters(ant, meta, parameterMap));
	}
		
	default TaggableColumn[] parseColumns(RequestQueryParam ant, TableMetadata metadata, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDescriptor::name);
		var cols = parameterMap.get(ant.columnParameter());
		if(isEmpty(cols) && isEmpty(ant.defaultColumns())) {
			throw new IllegalArgumentException("require " + ant.columnParameter() + " parameter");
		}
		var colStream = isEmpty(cols) //TD check first param isBlank
				? Stream.of(ant.defaultColumns())
				: flatStream(cols);
		return colStream.map(p->{
			var desc = map.get(toEnumName(p));
			if(desc != null) {
				var c = desc.from(this);
				if(c != null) {
					return c;
				}
			}
			throw columnNotFoundException(p);
		}).toArray(TaggableColumn[]::new);
	}
	
	default DBFilter[] parseFilters(RequestQueryParam ant, TableMetadata metadata, Map<String, String[]> parameterMap) {

		var map = toMap(DatabaseScanner.get().getColumns(), ColumnDescriptor::name);
    	var skipColumns = Set.of(ant.revisionParameter(), ant.columnParameter());
    	var filters = new LinkedList<DBFilter>();
		parameterMap.entrySet().stream()
		.filter(e-> !skipColumns.contains(e.getKey()))
		.forEach(p-> {
 			var desc = map.get(toEnumName(p.getKey()));
 			if(desc != null) {
				var c = desc.from(this);
				if(c instanceof TableColumn) {
					var filter = c.filter(desc.expression(metadata.column((TableColumn)c), flatArray(p.getValue())));
					filters.add(filter);
				}
				else if(c != null) {
					throw new UnsupportedOperationException("applying filter on " + p.getKey());//
				}
				else if(!ant.allowUnknownParameters()){
					throw columnNotFoundException(p.getKey());
				}
 			}
 			else if(!ant.allowUnknownParameters()) {
				throw columnNotFoundException(p.getKey());
 			}
 			//warn
		});
		return filters.toArray(DBFilter[]::new);
	}

	@Deprecated // move to client side 
	static String toEnumName(String v) {
		return v.replace("-", "_").toUpperCase();
	}
	
    static String[] flatArray(String... arr) {
    	return flatStream(arr).toArray(String[]::new);
    }

    static Stream<String> flatStream(String... arr) { //number local separator
    	return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
    }
}
