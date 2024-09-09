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

	Optional<NamedColumn> lookupDeclaredColumn(String name);

	QueryView overView(DBView view, Supplier<QueryView> supp);
	
	default QueryView overView(DBView view) {
		return overView(view, ()-> new QueryBuilder().columns(allColumns(view)).asView());
	}
}