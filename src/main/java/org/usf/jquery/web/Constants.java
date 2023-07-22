package org.usf.jquery.web;

import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constants {
	
	static final String COLUMN = "column";
	static final String COLUMN_DISTINCT = "column.distinct";
	static final String ORDER = "order";
	static final String REVISION = "revision"; //not standard
	
	static final Set<String> RESERVED_WORDS = Set.of(COLUMN, COLUMN_DISTINCT, ORDER, REVISION);

}
