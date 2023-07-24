package org.usf.jquery.web;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DatabaseMetadata {
	
	static final DatabaseMetadata EMPTY_DATABASE = new DatabaseMetadata(emptyMap(), emptyMap());

	private final Map<String, TableDecoratorWrapper> tableMap;
	private final Map<String, ColumnDecoratorWrapper> columnMap;

	public boolean declaredTable(String id) {
		return tableMap.containsKey(id);
	}
	
	public TableDecorator getTable(String id) {
		var table = tableMap.get(id);
		if(nonNull(table)) {
			return table;
		}
		throw new NoSuchElementException(id + " not found");
	}
	
	public boolean declaredColumn(String id) {
		return columnMap.containsKey(id);
	}
	
	public ColumnDecorator getColumn(String id) {
		var column = columnMap.get(id);
		if(nonNull(column)) {
			return column;
		}
		throw new NoSuchElementException(id + " not found");
	}
	
	Collection<TableDecoratorWrapper> tables() {
		return tableMap.values();
	}

	Collection<ColumnDecoratorWrapper> columns() {
		return columnMap.values();
	}
}

