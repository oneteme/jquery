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
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.COLUMN_DISTINCT;
import static org.usf.jquery.web.Parameters.FETCH;
import static org.usf.jquery.web.Parameters.JOIN;
import static org.usf.jquery.web.Parameters.OFFSET;
import static org.usf.jquery.web.Parameters.ORDER;
import static org.usf.jquery.web.Parameters.VIEW;
import static org.usf.jquery.web.RequestParser.parseEntries;
import static org.usf.jquery.web.RequestParser.parseEntry;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryBuilder;
import org.usf.jquery.core.TableView;
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
	
	default JoinBuilder join(String name) {
		return null; //no join by default
	}
	
	default PartitionBuilder partition(String name) {
		return null; //no partition by default
	}
	
	default DBView view() {
		return metadata().getView();
	}
	
	default NamedColumn column(@NonNull ColumnDecorator cd) {//final
		var meta = metadata().columnMetadata(cd);
		if(nonNull(meta)) {
			return new ViewColumn(meta.getName(), view(), meta.getType(), cd.reference(this));
		}
		var b = cd.builder(this);
		if(nonNull(b)) {
			return b.build(this).as(cd.reference(this), cd.type(this));
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
		return currentContext().computeTableMetadata(this, cols-> 
			new ViewMetadata(requireNonNull(builder(), identity() + ".builder").build(), declaredColumns(this, cols)));
	}
	
	static Map<String, ColumnMetadata> declaredColumns(ViewDecorator vd, Collection<ColumnDecorator> cols){
		return cols.stream().<Entry<String,ColumnMetadata>>mapMulti((cd, acc)-> ofNullable(vd.columnName(cd))
						.map(cn-> entry(cd.identity(), columnMetadata(cn, cd.type(vd))))
						.ifPresent(acc)) //view column only
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}
	
	default QueryBuilder query(Map<String, String[]> parameterMap) {
		var query = new QueryBuilder(currentContext().getMetadata().getType());
		parseViews(query, parameterMap);
		parseColumns(query, parameterMap);
		parseOrders(query, parameterMap);
		parseJoin(query, parameterMap);
		parseFetch(query, parameterMap);
		parseOffset(query, parameterMap);
		parseFilters(query, parameterMap); //remove all entries before parse filters
		return query;
	}
	
	default void parseViews(QueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(VIEW)) {
			Stream.of(parameters.remove(VIEW))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> currentContext().declareView(e.evalView(this, query)));
		}
	}
	
	default void parseColumns(QueryBuilder query, Map<String, String[]> parameters) {
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
			.map(e-> (NamedColumn) e.evalColumn(this, query, true))
			.forEach(query::columns);
		}
		else {
			throw new IllegalArgumentException(format("requrie %s or %s parameter", COLUMN, COLUMN_DISTINCT));
		}
	}

	default void parseOrders(QueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			Stream.of(parameters.remove(ORDER))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> query.orders(e.evalOrder(this, query)));
		}
	}
	default void parseJoin(QueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(JOIN)) {
			Stream.of(parameters.remove(JOIN))
			.flatMap(c-> parseEntries(c).stream())
			.forEach(e-> query.joins(e.evalJoin(this, query)));
		}
	}

	default void parseFetch(QueryBuilder query, Map<String, String[]> parameters) {
		requirePositiveInt(FETCH, parameters).ifPresent(query::fetch);
	}
	
	default void parseOffset(QueryBuilder query, Map<String, String[]> parameters) {
		requirePositiveInt(OFFSET, parameters).ifPresent(query::offset);
	}
	
	default void parseFilters(QueryBuilder query, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.flatMap(e-> {
    		var re = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> re.evalFilter(this, query, parseEntries(v)));
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
