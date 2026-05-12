package org.usf.jquery.core;

import static java.util.Arrays.stream;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
public enum Provider {

	POSTGRESQL, MYSQL, ORACLE, SQLSERVER, DEFAULT, 
	
	TERADATA, H2;
	
	public static Provider parseName(@NonNull String name) {
		var v = name.toUpperCase();
		return stream(values())
				.filter(d-> d.name().contains(v))
				.findAny()
				.orElseThrow(()-> new IllegalArgumentException("unsupported database product: " + name));
	}
}
