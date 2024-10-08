package org.usf.jquery.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Parameters {

	public static final String DATABASE = "database";
	public static final String QUERY = "query";
	public static final String VIEW = "view";
	public static final String SELECT = "select";
	public static final String COLUMN = "column"; //select
	public static final String DISTINCT = "distinct";  //select.distinct
	public static final String COLUMN_DISTINCT = "column.distinct";  //select.distinct
	public static final String FILTER = "filter";
	public static final String ORDER = "order";
	public static final String OFFSET = "offset";
	public static final String LIMIT = "limit";
	public static final String JOIN = "join";
	public static final String PARTITION = "partition";
}
