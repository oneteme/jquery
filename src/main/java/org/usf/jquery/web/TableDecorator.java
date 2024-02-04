package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.currentDatabase;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.FETCH;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.JQueryContext.database;
import static org.usf.jquery.web.MissingParameterException.missingParameterException;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;
import static org.usf.jquery.web.RequestParser.parseArgs;
import static org.usf.jquery.web.RequestParser.parseEntries;
import static org.usf.jquery.web.RequestParser.parseEntry;
import static org.usf.jquery.web.TableMetadata.emptyMetadata;
import static org.usf.jquery.web.TableMetadata.tableMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TaggableView;
import org.usf.jquery.core.ViewColumn;

/**
 * 
 * @author u$f
 *
 */
public interface TableDecorator {
	
	String identity(); //URL
	
	String tableName(); //SQL check schema.table
	
	Optional<String> columnName(ColumnDecorator cd);
	
	default TaggableView table() { //optim
		var b = builder();
		return nonNull(b) 
				? b.build().as(identity())
				: new DBTable(tableName(), identity());
	}
	
	default TaggableColumn column(ColumnDecorator cd) {
		var b = cd.builder();
		if(nonNull(b)) {
			return b.build(this).as(cd.reference());
		}
		var cn = columnName(cd).orElseThrow(()-> undeclaredResouceException(identity(), cd.identity()));
		return new ViewColumn(table(), requireLegalVariable(cn), cd.reference(), cd.dataType(this));
	}
	
	default ViewBuilder builder() {
		return null; // no builder by default
	}

	default CriteriaBuilder<DBFilter> criteria(String name) { //!aggregation 
		return null;  // no criteria by default
	}
	
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		currentDatabase(database().getType()); //table database
		var query = new RequestQueryBuilder();
		parseColumns(query, parameterMap);
		parseFilters(query, parameterMap);
		parseOrders (query, parameterMap);
		parseFetch(query, parameterMap);
		return query;
	}
	
	default void parseColumns(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(COLUMN_DISTINCT) && parameters.containsKey(COLUMN)) {
			throw new IllegalArgumentException("cannot use both parameters " + quote(COLUMN_DISTINCT) + " and " + quote(COLUMN));
		}
		var cols = parameters.containsKey(COLUMN_DISTINCT) 
				? parameters.get(COLUMN_DISTINCT) 
				: parameters.get(COLUMN); //can be combined in PG (distinct on)
		if(isEmpty(cols)) {
			throw missingParameterException(COLUMN, COLUMN_DISTINCT);
		}
		if(parameters.containsKey(COLUMN_DISTINCT)){
			query.distinct();
		}
		Stream.of(cols)
		.flatMap(v-> parseEntries(v).stream())
		.forEach(e-> query.columns(e.evalColumn(this)));
	}

	default void parseFilters(RequestQueryBuilder query, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.filter(e-> !RESERVED_WORDS.contains(e.getKey()))
    	.flatMap(e-> {
    		var re = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> re.evalFilter(this, parseArgs(v)));
    	})
    	.forEach(query::filters);
	}

	default void parseOrders(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			Stream.of(parameters.get(ORDER))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> query.orders(e.evalOrder(this)));
		}
	}
	
	default void parseFetch(RequestQueryBuilder query, Map<String, String[]> parameters) {
		query.fetch(requirePositiveInt(OFFSET, parameters), 
				requirePositiveInt(FETCH, parameters));
	}
	
	private static Integer requirePositiveInt(String key, Map<String, String[]> parameters) {
		if(parameters.containsKey(key)) {
			var values = parameters.get(key);
			if(values.length == 1) {
				var v = parseInt(values[0]);
				if(v >= 0) {
					return v;
				}
				throw new IllegalArgumentException(key + " cannot be negative");
			}
			throw new IllegalArgumentException("too many value");
		}
		return null;
	}

	default TableMetadata metadata() {
		return database().tableMetada(this) 
				.orElseGet(()-> emptyMetadata(this)); // not binded
	}
	
	default TableMetadata createMetadata(Collection<ColumnDecorator> columns) {
		return tableMetadata(this, columns);
	}

	static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}
}
