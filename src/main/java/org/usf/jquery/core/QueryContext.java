package org.usf.jquery.core;

import static org.usf.jquery.core.DBColumn.allColumns;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 
 * @author u$f
 *
 */
public interface QueryContext {

	Optional<TaggableColumn> declaredColumn(String name);

	QueryView overView(DBView view, Supplier<QueryView> supp);
	
	default QueryView overView(DBView view) {
		return overView(view, ()-> new RequestQueryBuilder().columns(allColumns(view)).asView());
	}
	
	default ViewColumn overView(DBView view, TaggableColumn column) {
		overView(view).getBuilder().columns(column);
		return new ViewColumn(view, column.tagname(), null, column.getType());
	}
	//sub query filters, orders, ...
}