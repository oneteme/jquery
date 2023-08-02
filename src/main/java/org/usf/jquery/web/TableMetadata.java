package org.usf.jquery.web;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
@RequiredArgsConstructor
final class TableMetadata {
	
	private final String name;
	private final Map<String, ColumnMetadata> columns = new LinkedHashMap<>();
	
	public void put(ColumnDecorator cd, ColumnMetadata cm) {
		columns.put(cd.identity(), cm);
	}
	
	public void putAll(Map<String, ColumnMetadata> columns) {
		this.columns.putAll(columns);
	}
	
	public boolean containsKey(ColumnDecorator cd) {
		return columns.containsKey(cd.identity());
	}
	
	public ColumnMetadata get(ColumnDecorator cd) {
		return columns.get(cd.identity());
	}
	
	public void clear() {
		columns.clear();
	}
	
	void requireColumns(TableDecorator table, Collection<ColumnDecorator> declaredCols) {
		if(columns.size() < declaredCols.size()) {
			throw new NoSuchElementException(declaredCols.stream()
					.filter(c-> !columns.containsKey(c.identity()))
					.map(table::columnName)
					.map(Optional::get) // getOrThrow
					.collect(joining(", ", "column(s) [", "] not found in " + name)));
		}
		else if(columns.size() > declaredCols.size()) {
			throw new IllegalStateException("illegal state");
		}
	}
	
	static void requireSameStructure(Collection<ColumnDecorator> declaredCols, Collection<TableMetadata> tables) {
		for(var cd : declaredCols) {
			var map = tables.stream().collect(groupingBy(t-> t.get(cd)));
			if(map.size() > 1) {
				map.entrySet().forEach(e-> 
				log.warn("column {} defined in table(s) [{}]", e.getKey(), e.getValue().stream()
						.map(TableMetadata::getName)
						.collect(joining(", "))));
				throw new IllegalStateException("pretty msg"); //TODO
			}
		}
	}

}
