package org.usf.jquery.web;

import static java.util.Arrays.asList;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
	
	public static final String COLUMN = "column";
	public static final String COLUMN_DISTINCT = "column.distinct"; 
	public static final String ORDER = "order";
//	static final String FORMAT = "format"; //CSV|JSON|ASCII
//	static final String FORMAT_SEPARATOR = "format.separator"; //only for CSV COMA|SEMICOLON
	public static final String REVISION = "revision"; //not standard
	public static final String REVISION_MODE = "revision.mode"; //not standard

	public static final String WINDOW_PARTITION = "window.partition";
	public static final String WINDOW_ORDER = "window.order";
	
	public static final String PARTITION = "partition"; //not res
	
	static final Set<String> RESERVED_WORDS = 
			Set.of(COLUMN, COLUMN_DISTINCT, ORDER, 
					WINDOW_PARTITION, WINDOW_ORDER, 
					REVISION, REVISION_MODE);
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0]; //not standard
	
	static final List<ArgumentParser> PARSERS = asList(
//			Boolean::parseBoolean, return false
			Long::parseLong, // byte, short, integer
			Double::parseDouble, // float
			v-> Date.valueOf(LocalDate.parse(v)),
			v-> Time.valueOf(LocalTime.parse(v)),
			v-> Timestamp.from(Instant.parse(v)));

}
