package org.usf.jquery.web;

import static java.util.Collections.emptyMap;

import java.util.Collection;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class DatabaseMetadata {
	
	static final DatabaseMetadata EMPTY_DATABASE = new DatabaseMetadata(emptyMap(), emptyMap());

	private final Map<String, TableDecoratorWrapper> tableMap;
	private final Map<String, ColumnDecoratorWrapper> columnMap;

	public boolean isDeclaredTable(String id) {
		return tableMap.containsKey(id);
	}
	
	@Deprecated
	public YearTableDecorator getYearTable(String id) {
		return (YearTableDecorator) getTable(id);
	}
	
	public TableDecorator getTable(String id) {
		return tableMap.computeIfAbsent(id, 
				NoSuchResourceException::throwNoSuchTableException);
	}
	
	public boolean isDeclaredColumn(String id) {
		return columnMap.containsKey(id);
	}
	
	public ColumnDecorator getColumn(String id) {
		return columnMap.computeIfAbsent(id, 
				NoSuchResourceException::throwNoSuchColumnException);
	}
	
	Collection<TableDecoratorWrapper> tables() {
		return tableMap.values();
	}

	Collection<ColumnDecoratorWrapper> columns() {
		return columnMap.values();
	}
}

