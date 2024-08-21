package org.usf.jquery.core;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 * 
 */
public enum Database {

	MYSQL, POSTGRESQL, ORACLE, SQLSERVER, TERADATA, H2;
	
	private static final ThreadLocal<Database> local = new ThreadLocal<>();

	public static Database currentDatabase() {
		return local.get();
	}

	static void setCurrentDatabase(Database db) {
		local.set(db);
	}

	public static Optional<Database> of(String name) {
		var v = name.toUpperCase();
		return Stream.of(values())
		.filter(d-> v.contains(d.name()))
		.findAny();
	}
}
