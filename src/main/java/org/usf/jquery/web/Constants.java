package org.usf.jquery.web;

import java.time.YearMonth;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constants {
	
	static final String COLUMN = "column"; //columns=collection1
	static final String COLUMN_DISTINCT = "column.distinct";
	static final String ORDER = "order";
	static final String REVISION = "revision"; //not standard
	static final String REVISION_MODE = "revision.mode"; //not standard
	
	static final Set<String> RESERVED_WORDS = 
			Set.of(COLUMN, COLUMN_DISTINCT, ORDER, REVISION, REVISION_MODE);
	
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0]; //not standard

}
