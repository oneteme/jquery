package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.FETCH;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.DatabaseManager.currentDatabase;
import static org.usf.jquery.web.MissingParameterException.missingParameterException;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;
import static org.usf.jquery.web.RequestContext.clearContext;
import static org.usf.jquery.web.RequestContext.currentContext;
import static org.usf.jquery.web.RequestParser.parseArgs;
import static org.usf.jquery.web.RequestParser.parseEntries;
import static org.usf.jquery.web.RequestParser.parseEntry;

import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBQuery;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.Utils;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public interface ViewDecorator {
	
	String identity(); //URL
	
	String columnName(ColumnDecorator cd);
	
	default ViewBuilder builder() {
		return null; // no builder by default
	}

	default CriteriaBuilder<DBFilter> criteria(String name) { //!aggregation 
		return null; // no criteria by default
	}
	
	default DBView table() {
		return metadata().getView(); //cached view
	}
	
	default TaggableColumn column(@NonNull ColumnDecorator cd) {
		var c = metadata().columnMetada(cd); //priority order
		if(nonNull(c)) {
			return c.getColumn();
		}
		var b = cd.builder(this);
		if(nonNull(b)) {
			return b.build(this).as(cd.reference(this)); //set type
		}
		throw undeclaredResouceException(identity(), cd.identity());
	}

	default ViewMetadata metadata() {
		return currentDatabase().viewMetadata(this);
	}
	
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		try {
			Utils.currentDatabase(currentDatabase().getType()); //table database
			var query = new RequestQueryBuilder();
			parseViews(query, parameterMap);
			parseColumns(query, parameterMap);
			parseFilters(query, parameterMap);
			parseOrders (query, parameterMap);
			parseFetch(query, parameterMap);
			query.views(currentContext().popQueries().toArray(DBQuery[]::new));
			return query;
		}
		finally {
			clearContext();
		}
	}
	
	default void parseViews(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(VIEW)) {
			Stream.of(parameters.get(VIEW))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> currentContext().putViewDecorator(new QueryDecorator(e.evalQuery(this, true))));
		}
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

	static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}
}
