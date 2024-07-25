package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.quote;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseManager {
	
	private static final Map<String, DatabaseMetadata> DATABASES = new HashMap<>();
	private static final ThreadLocal<DatabaseMetadata> LOCAL_DB  = new ThreadLocal<>();
	
	public void register(DatabaseConfiguration config) {
		DATABASES.compute(config.getDatabase().identity(), (id,dm)->{
			if(isNull(dm)) {
				return new DatabaseMetadata(config);
			}
			throw new IllegalStateException("database configuration conflict " + quote(id));
		});
	}
	
	public static DatabaseMetadata currentDatabase() {
		var db = LOCAL_DB.get();
		if(nonNull(db)) {
			return db;
		}
		if(DATABASES.size() == 1) { //default database
			return DATABASES.values().iterator().next();
		}
		throw DATABASES.isEmpty()
				? new NoSuchElementException("no database configured")
				: new IllegalStateException("no database selected");
	}

	public static DatabaseMetadata switchDatabase(String name) {
		var db = DATABASES.get(name);
		if(nonNull(db)) {
			LOCAL_DB.set(db);
			return db;
		}
		throw new NoSuchElementException(quote(name) + " was not initialized");
	}
	
	public static void releaseDatabase() {
		LOCAL_DB.remove();
	}
}
