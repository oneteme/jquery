package org.usf.jquery.core;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 * 
 */
public enum Database {

	MYSQL, POSTGRESQL, ORACLE, SQLSERVER, TERADATA;
	
	public static Optional<Database> of(String name) {
		var v = name.toUpperCase();
		return Stream.of(values())
		.filter(d-> v.contains(d.name()))
		.findAny();
	}
	
}
