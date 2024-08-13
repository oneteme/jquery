package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.ColumnMetadata.columnMetadata;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.FETCH;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.MissingParameterException.missingParameterException;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;
import static org.usf.jquery.web.RequestParser.parseArgs;
import static org.usf.jquery.web.RequestParser.parseEntries;
import static org.usf.jquery.web.RequestParser.parseEntry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TableView;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.ViewColumn;

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
		return this::buildView;
	}

	default CriteriaBuilder<DBFilter> criteria(String name) { //!aggregation 
		return null; //no criteria by default
	}
	
	default JoinBuilder joiner(String name) {
		return null; //no builder by default
	}

	default DBView view() {//final
		return metadata().getView();
	}
	
	default TaggableColumn column(@NonNull ColumnDecorator cd) {//final
		var meta = metadata().columnMetadata(cd);
		if(nonNull(meta)) {
			return new ViewColumn(view(), meta.getName(), cd.reference(this), meta.getType());
		}
		var b = cd.builder(this);
		if(nonNull(b)) {
			return b.build(this).as(cd.reference(this)); //set type
		}
		throw undeclaredResouceException(cd.identity(), identity());
	}
	
	private TableView buildView() {
		var tn = currentContext().getDatabase().viewName(this);
		if(nonNull(tn)){
			var idx = tn.indexOf('.');
			return idx == -1 
					? new TableView(null, requireLegalVariable(tn), identity()) 
					: new TableView(requireLegalVariable(tn.substring(0, idx)),
							requireLegalVariable(tn.substring(idx, tn.length())), identity());
		}
		throw undeclaredResouceException(identity(), currentContext().getDatabase().identity());
	}

	default ViewMetadata metadata() {
		var view = requireNonNull(builder(), identity() + ".builder").build();
		return currentContext().computeTableMetadata(this, cols-> new ViewMetadata(view, 
				cols.stream().<Entry<String,ColumnMetadata>>mapMulti((cd, acc)-> ofNullable(columnName(cd))
						.map(cn-> entry(cd.identity(), columnMetadata(cn, cd.type(this))))
						.ifPresent(acc)) //view column only
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue))));
	}
	
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		var query = new RequestQueryBuilder();
		parseViews(query, parameterMap);
		parseColumns(query, parameterMap);
		parseOrders(query, parameterMap);
		parseFetch(query, parameterMap);
		parseFilters(query, parameterMap);
		return query;
	}
	
	default void parseViews(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(VIEW)) {
			Stream.of(parameters.remove(VIEW))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> currentContext().declareView(e.evalView(this)));
		}
	}
	
	default void parseColumns(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(COLUMN_DISTINCT) && parameters.containsKey(COLUMN)) {
			throw new IllegalArgumentException("cannot use both parameters " + quote(COLUMN_DISTINCT) + " and " + quote(COLUMN));
		}
		String[] cols;
		if(parameters.containsKey(COLUMN_DISTINCT)) {
			cols = parameters.remove(COLUMN_DISTINCT);
			query.distinct();
		}
		else {
			cols = parameters.remove(COLUMN);	
		}
		if(isEmpty(cols)) {
			throw missingParameterException(COLUMN, COLUMN_DISTINCT);
		}
		Stream.of(cols)
		.flatMap(v-> parseEntries(v).stream())
		.map(e-> e.evalColumn(this))
		.forEach(query::columns);
	}

	default void parseFilters(RequestQueryBuilder query, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
//    	.filter(e-> !RESERVED_WORDS.contains(e.getKey()))
    	.flatMap(e-> {
    		var re = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> re.evalFilter(this, parseArgs(v)));
    	})
    	.forEach(query::filters);
	}

	default void parseOrders(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			Stream.of(parameters.remove(ORDER))
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
			var values = parameters.remove(key);
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
