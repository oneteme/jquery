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

	private final Map<String, ViewMetadata> views; //lazy loading
	private Database type;
	
	public ColumnMetadata columnMetadata(ViewDecorator view, ColumnDecorator cd) {
		var meta = viewMetadata(view);
		return isNull(meta) ? null : meta.columnMetadata(cd);
	}
	
	public ViewMetadata viewMetadata(ViewDecorator view) {
		return views.get(view.identity());
	}
	
	public void fetch(DatabaseMetaData metadata, String schema) throws SQLException {
		type = Database.of(metadata.getDatabaseProductName()).orElse(null);
		if(nonNull(views)) {
			for(var v : views.values()) {
				v.fetch(metadata, schema);
			}
		}
	}
}