package org.usf.jquery.web;

import static java.util.Collections.synchronizedMap;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.Database;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter(AccessLevel.PACKAGE)
public final class DatabaseMetadata {

	private final Map<String, ViewMetadata> tables = synchronizedMap(new LinkedHashMap<>()); //lazy loading
	private Database type;
	
	public void fetch(DatabaseMetaData metadata) throws SQLException {
		type = Database.of(metadata.getDatabaseProductName()).orElse(null);
	}
}

