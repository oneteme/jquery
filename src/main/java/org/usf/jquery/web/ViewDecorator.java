package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
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
import static org.usf.jquery.web.Constants.JOIN;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.RequestParser.parseArgs;
import static org.usf.jquery.web.RequestParser.parseEntries;
import static org.usf.jquery.web.RequestParser.parseEntry;
import static org.usf.jquery.web.ResourceAccessException.undeclaredResouceException;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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

	default DBView view() {
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
		return currentContext().computeTableMetadata(this, cols-> 
			new ViewMetadata(view, declaredColumns(this, cols)));
	}
	
	static Map<String, ColumnMetadata> declaredColumns(ViewDecorator vd, Collection<ColumnDecorator> cols){
		return cols.stream().<Entry<String,ColumnMetadata>>mapMulti((cd, acc)-> ofNullable(vd.columnName(cd))
						.map(cn-> entry(cd.identity(), columnMetadata(cn, cd.type(vd))))
						.ifPresent(acc)) //view column only
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}
	
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		var query = new RequestQueryBuilder(currentContext().getMetadata().getType());
		parseViews(query, parameterMap);
		parseColumns(query, parameterMap);
		parseOrders(query, parameterMap);
		parseJoin(query, parameterMap);
		parseFetch(query, parameterMap);
		parseOffset(query, parameterMap);
		parseFilters(query, parameterMap); //remove all entries before parse filters
		query.overViews(currentContext().getOverView()); //over clause: after filters 
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
		if(parameters.containsKey(COLUMN) && parameters.containsKey(COLUMN_DISTINCT)) {
			throw new IllegalStateException("both parameters are present " + quote(COLUMN_DISTINCT) + " and " + quote(COLUMN));
		}
		String[] cols;
		if(parameters.containsKey(COLUMN_DISTINCT)) {
			cols = parameters.remove(COLUMN_DISTINCT);
			query.distinct();
		}
		else {
			cols = parameters.remove(COLUMN);	
		}
		if(!isEmpty(cols)) {
			Stream.of(cols)
			.flatMap(v-> parseEntries(v).stream())
			.map(e-> (TaggableColumn) e.evalColumn(this, true, true))
			.forEach(query::columns);
		}
		else {
			throw new IllegalArgumentException(format("requrie %s or %s parameter", COLUMN, COLUMN_DISTINCT));
		}
	}

	default void parseOrders(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			Stream.of(parameters.remove(ORDER))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> query.orders(e.evalOrder(this)));
		}
	}
	default void parseJoin(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(JOIN)) {
			Stream.of(parameters.remove(JOIN))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> query.joins(e.evalJoin(this)));
		}
	}

	default void parseFetch(RequestQueryBuilder query, Map<String, String[]> parameters) {
		requirePositiveInt(FETCH, parameters).ifPresent(query::fetch);
	}
	
	default void parseOffset(RequestQueryBuilder query, Map<String, String[]> parameters) {
		requirePositiveInt(OFFSET, parameters).ifPresent(query::offset);
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
	
	private static Optional<Integer> requirePositiveInt(String key, Map<String, String[]> parameters) {
		if(parameters.containsKey(key)) {
			var values = parameters.remove(key);
			if(values.length == 1) {
				var v = parseInt(values[0]);
				if(v >= 0) {
					return Optional.of(v);
				}
				throw new IllegalArgumentException(key + " cannot be negative");
			}
			throw new IllegalArgumentException("too many value");
		}
		return Optional.empty();
	}
	
	static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}
}
