package org.usf.jquery.web;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public final class DatabaseMetadata {
	
	private final Map<TableDecorator, TableMetadata> tables;
	
	public TableMetadata table(TableDecorator table) {
		return tables.get(table);
	}
}