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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constants {
	
	static final String COLUMN = "column";
	static final String COLUMN_DISTINCT = "column.distinct"; 
	static final String ORDER = "order";
//	static final String FORMAT = "format"; //CSV|JSON|ASCII
//	static final String FORMAT_SEPARATOR = "format.separator"; //only for CSV COMA|SEMICOLON
	static final String REVISION = "revision"; //not standard
	static final String REVISION_MODE = "revision.mode"; //not standard
	
	static final Set<String> RESERVED_WORDS = 
			Set.of(COLUMN, COLUMN_DISTINCT, ORDER, REVISION, REVISION_MODE);
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0]; //not standard
	
	static final List<ArgumentParser> PARSERS = asList(
			Boolean::parseBoolean,
			Long::parseLong, // byte, short, integer
			Double::parseDouble, // float
			v-> Date.valueOf(LocalDate.parse(v)),
			v-> Time.valueOf(LocalTime.parse(v)),
			v-> Timestamp.from(Instant.parse(v)));

}
