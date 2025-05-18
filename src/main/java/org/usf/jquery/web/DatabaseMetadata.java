package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.usf.jquery.core.Database;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
public final class DatabaseMetadata {

	private final Map<String, ViewMetadata> tables; //lazy loading
	private Database type;
	
	public ViewMetadata viewMetadata(ViewDecorator view) {
		return tables.get(view.identity());
	}
	
	public ColumnMetadata columnMetadata(ViewDecorator view, ColumnDecorator cd) {
		var tm = viewMetadata(view);
		return isNull(tm) ? null : tm.columnMetadata(cd);
	}
	
	public void fetch(DatabaseMetaData metadata, String schema) {
		try {
			type = Database.of(metadata.getDatabaseProductName()).orElse(null);
			if(nonNull(tables)) {
				for(var table : tables.values()) {
					table.fetch(metadata, schema);
				}
			}
		}
		catch (SQLException e) {
			log.warn("error while scanning database metadata", e);
		}
	}
}