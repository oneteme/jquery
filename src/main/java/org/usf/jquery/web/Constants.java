package org.usf.jquery.web;

import java.time.YearMonth;
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
	public static final String REVISION = "revision"; //not standard
	public static final String REVISION_MODE = "revision.mode"; //not standard
	public static final String PARTITION = "partition"; //not res
	
	static final Set<String> RESERVED_WORDS = 
			Set.of(COLUMN, COLUMN_DISTINCT, ORDER, REVISION, REVISION_MODE);
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0]; //not standard
	
}
