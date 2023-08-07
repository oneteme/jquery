package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.Constants.WINDOW_ORDER;
import static org.usf.jquery.web.Constants.WINDOW_PARTITION;
import static org.usf.jquery.web.CriteriaBuilder.ofComparator;
import static org.usf.jquery.web.JQueryContext.database;
import static org.usf.jquery.web.MissingParameterException.missingParameterException;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;
import static org.usf.jquery.web.RequestColumn.decodeColumn;
import static org.usf.jquery.web.RequestFilter.decodeFilter;
import static org.usf.jquery.web.TableMetadata.emptyMetadata;
import static org.usf.jquery.web.TableMetadata.tableMetadata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.DBWindow;
import org.usf.jquery.core.InCompartor;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

/**
 * 
 * @author u$f
 *
 */
public interface TableDecorator {
	
	String identity(); //URL
	
	String tableName(); //SQL
	
	Optional<String> columnName(ColumnDecorator cd);
	
	default Optional<String> schema() {
		return empty();
	}
	
	default int columnType(ColumnDecorator cd) {
		return database().columnMetada(this, cd)
				.map(ColumnMetadata::getDataType)
				.orElse(AUTO_TYPE); //not binded
	}
	
	default DBTable table() {
		return new DBTable(schema().orElse(null), tableName(), identity());
	}
	
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		var query = new RequestQueryBuilder();
		parseWindow (query, parameterMap);
		parseColumns(query, parameterMap);
		parseFilters(query, parameterMap);
		parseOrders (query, parameterMap);
		return query;
	}
	
	default void parseWindow(RequestQueryBuilder query, Map<String, String[]> parameters) {
		var map = new LinkedHashMap<String, DBWindow>();
		if(parameters.containsKey(WINDOW_PARTITION)) {
			flatParameters(parameters.get(WINDOW_PARTITION)).forEach(p->{
				var rc = decodeColumn(p, this, false);
				var td = rc.tableDecorator();
				map.computeIfAbsent(td.identity(), k-> new DBWindow(td.table()))
				.partitions(td.column(rc.columnDecorator()));
			});
		}
		if(parameters.containsKey(WINDOW_ORDER)) {
			flatParameters(parameters.get(WINDOW_ORDER)).forEach(p->{
				var rc = decodeColumn(p, this, true);
				var td = rc.tableDecorator();
				var col = rc.tableDecorator().column(rc.columnDecorator());
				map.computeIfAbsent(td.identity(), k-> new DBWindow(td.table()))
				.orders(isNull(rc.expression()) 
						? col.order() 
						: col.order(parseOrder(rc.expression())));
			});
		}
		if(map.isEmpty()) {
			query.tables(table());
		}
		else {
			map.values().forEach(w-> query.tables(w).filters(w.filter()));
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
		}//TD check first param !isBlank
		flatParameters(cols).forEach(p->{
			var rc = decodeColumn(p, this, false);
			var td = rc.tableDecorator();
			query.tablesIfAbsent(td.table()).columns(td.column(rc.columnDecorator()));
		});
	}

	default void parseFilters(RequestQueryBuilder query, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.filter(e-> !RESERVED_WORDS.contains(e.getKey()))
    	.forEach(e-> {
    		var rf = decodeFilter(e, this);
    		query.tablesIfAbsent(rf.tables()).filters(rf.filters());
    	});
	}

	default void parseOrders(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			flatParameters(parameters.get(ORDER)).forEach(p->{
				var rc = decodeColumn(p, this, true);
				var col = rc.tableDecorator().column(rc.columnDecorator());
				query.tablesIfAbsent(rc.tableDecorator().table())
				.orders(isNull(rc.expression()) 
						? col.order() 
						: col.order(parseOrder(rc.expression())));
			});
		}
	}

	default TaggableColumn column(ColumnDecorator column) {
		if(nonNull(column.builder())) {
			return column.builder().column(this).as(column.reference());
		}
		var cn = columnName(column);
		if(cn.isPresent()) {
			return new TableColumn(requireLegalVariable(cn.get()), column.reference(), identity());
		}
		throw undeclaredResouceException(identity(), column.identity());
	}
	
	//expression => criteria | comparator
	default ComparisonExpression expression(ColumnDecorator column, String expres, String... values) {
		var criteria = column.criteria(expres);
		if(nonNull(criteria)) {
			return criteria.build(values);
		}
		var cmp = column.comparator(expres, values.length);
		if(nonNull(cmp)) {
			var type = column.dataType();
			if(type == AUTO_TYPE) { // logical column type can be set in table
				type = columnType(column);
			}//else : overridden
	    	var prs = requireNonNull(column.parser(type));
	    	if(values.length == 1) {
	    		return cmp.expression(prs.parseValue(values[0]));
	    	}
			return cmp instanceof InCompartor 
					? cmp.expression(prs.parseValues(values))
					: ofComparator(cmp).build(prs.parseValues(values));
		}
		throw cannotEvaluateException("expression", expres);
	}
	
	default TableMetadata metadata() {
		return database().tableMetada(this) 
				.orElseGet(()-> emptyMetadata(this)); // not binded
	}
	
	default TableMetadata createMetadata(Collection<ColumnDecorator> columns) {
		return tableMetadata(this, columns);
	}
	
	static String parseOrder(String order) {
		if("desc".equals(order) || "asc".equals(order)) {
			return order.toUpperCase();
		}
		throw cannotEvaluateException(ORDER, order);
	}

	static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}
}
