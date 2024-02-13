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

	public static final String VIEW = "view";
	public static final String SELECT = "select"; //select
	public static final String COLUMN = "column"; //select
	public static final String DISTINCT = "distinct";  //select.distinct
	public static final String COLUMN_DISTINCT = "column.distinct";  //select.distinct
	public static final String FILTER = "filter";
	public static final String ORDER = "order";
	public static final String FETCH = "fetch";
	public static final String OFFSET = "offset";
	public static final String PARTITION = "partition";
	public static final String REVISION = "revision"; //not standard
	public static final String REVISION_MODE = "revision.mode"; //not standard
	
	static final Set<String> RESERVED_WORDS = 
			Set.of(VIEW, COLUMN, COLUMN_DISTINCT, FILTER, ORDER, OFFSET, FETCH, 
					PARTITION, REVISION, REVISION_MODE); //metadata ?
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0]; //not standard
	
}
