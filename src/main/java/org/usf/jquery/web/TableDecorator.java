package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.RESERVED_WORDS;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.JQueryContext.database;
import static org.usf.jquery.web.MissingParameterException.missingParameterException;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;
import static org.usf.jquery.web.RequestColumn.decodeColumns;
import static org.usf.jquery.web.RequestColumn.decodeSingleColumn;
import static org.usf.jquery.web.RequestFilter.decodeFilter;
import static org.usf.jquery.web.TableMetadata.emptyMetadata;
import static org.usf.jquery.web.TableMetadata.tableMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.OverColumn;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.SQLType;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.WindowView;

/**
 * 
 * @author u$f
 *
 */
public interface TableDecorator {
	
	String identity(); //URL
	
	String tableName(); //SQL check schema.table 
	
	Optional<String> columnName(ColumnDecorator cd);
	
	default DBTable table() {
		return new DBTable(tableName(), identity());
	}
	
	default Optional<SQLType> columnType(ColumnDecorator cd) {
		return metadata().columnMetada(cd)
				.map(ColumnMetadata::getDataType); //else not binded
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
	
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		var query = new RequestQueryBuilder();
		parseViews (query, parameterMap);
		parseColumns(query, parameterMap);
		parseFilters(query, parameterMap);
		parseOrders (query, parameterMap);
		return query;
	}
	
	default void parseViews(RequestQueryBuilder query, Map<String, String[]> parameters) {
		var exp = "(" + join("|", "rank", "row_number", "dense_rank") + ")" + "(\\(\\))?\\.over.*"; //almost
		for(var t : context().tables()) {
			var pr = "^(" + t.identity() + "\\.)";
			if(t == this) {
				pr += "?";
			}
			var pattern = pr + exp;
			var c = parameters.entrySet().stream()
			.filter(e-> e.getKey().matches(pattern))
			.collect(toList());
			if(!c.isEmpty()) {
				if(c.size() == 1 && c.get(0).getValue().length == 1) {
					var entry = c.get(0);
					var rc = decodeSingleColumn(entry.getKey(), this, true); //allow comparator
					var nc = (NamedColumn) rc.toColumn();
					var oc = nc.unwrap();
					if(oc instanceof OverColumn) {
						var wv = new WindowView(rc.tableDecorator().table(), (OverColumn) oc, 
								nc.tagname(), rc.expression(entry.getValue()));
						query.tables(wv).filters(wv.filter());
						parameters.remove(entry.getKey());
					}
					else {
						throw new IllegalStateException("OverColumn expected");
					}
				}
				else {
					throw new UnsupportedOperationException("multiple window function");
				}
			}
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
		.flatMap(c-> decodeColumns(c, this, false))
		.forEach(rc-> query.tablesIfAbsent(rc.tableDecorator().table()).columns(rc.toColumn()));
	}

	default void parseFilters(RequestQueryBuilder query, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.filter(e-> !RESERVED_WORDS.contains(e.getKey()))
    	.map(e-> decodeFilter(e, this))
    	.forEach(rf-> query.tablesIfAbsent(rf.tables()).filters(rf.filters()));
	}

	default void parseOrders(RequestQueryBuilder query, Map<String, String[]> parameters) {
		if(parameters.containsKey(ORDER)) {
			Stream.of(parameters.get(ORDER))
			.flatMap(c-> decodeColumns(c, this, true))
			.forEach(rc-> query.tablesIfAbsent(rc.tableDecorator().table()).orders(rc.toOrder()));
		}
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
