package org.usf.jquery.web;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchColumnException;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchTableException;

import java.util.Collection;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Resource {
	
	private final Map<String, TableDecorator> tables;
	private final Map<String, ColumnDecorator> columns;

	public boolean isDeclaredTable(String id) {
		return tables.containsKey(id);
	}

	public TableDecorator getTable(String value) {
		return ofNullable(tables.get(value)).orElseThrow(()-> throwNoSuchTableException(value));
	}
	
	public boolean isDeclaredColumn(String id) {
		return columns.containsKey(id);
	}

	public ColumnDecorator getColumn(String value) {
		return ofNullable(columns.get(value)).orElseThrow(()-> throwNoSuchColumnException(value));
	}

	public static Resource init(Collection<TableDecorator> tables, Collection<ColumnDecorator> columns) {
		return new Resource(
				tables.stream().collect(toUnmodifiableMap(TableDecorator::identity, identity())),
				columns.stream().collect(toUnmodifiableMap(ColumnDecorator::identity, identity())));
	}
	
	public static Resource emptyResource() {
		return new Resource(emptyMap(), emptyMap());
	}
	
}
