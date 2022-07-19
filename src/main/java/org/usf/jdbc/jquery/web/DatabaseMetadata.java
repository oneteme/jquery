package org.usf.jdbc.jquery.web;

import static org.usf.jdbc.jquery.web.TableMetadata.defaultTableMetadata;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@RequiredArgsConstructor
public final class DatabaseMetadata {
	
	private final Map<String, TableMetadata> tables;
	
	public TableMetadata table(TableDescriptor table) {
		var meta = tables.get(table.name());
		if(meta != null) {
			return meta;
		}
		log.warn("table metadata not found : " + table.name());
		return defaultTableMetadata();
	}
}