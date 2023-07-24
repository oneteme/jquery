package org.usf.jquery.web;

import static java.util.Collections.emptyMap;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

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
	
	public TableDecorator getTable(String id) {
		return tableMap.computeIfAbsent(id, k-> {throw new NoSuchElementException(k + " table not found");});
	}
	
	public boolean isDeclaredColumn(String id) {
		return columnMap.containsKey(id);
	}
	
	public ColumnDecorator getColumn(String id) {
		return columnMap.computeIfAbsent(id, k-> {throw new NoSuchElementException(k + " column not found");});
	}
	
	Collection<TableDecoratorWrapper> tables() {
		return tableMap.values();
	}

	Collection<ColumnDecoratorWrapper> columns() {
		return columnMap.values();
	}
}

