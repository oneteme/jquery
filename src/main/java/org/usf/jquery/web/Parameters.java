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

	//parameters
	public static final String VIEW_PARAM = "view";
	public static final String COLUMN_PARAM = "column";
	public static final String DISTINCT_PARAM = "distinct";
	public static final String JOIN_PARAM = "join";
	public static final String ORDER_PARAM = "order";
	public static final String LIMIT_PARAM = "limit";
	public static final String OFFSET_PARAM = "offset";
	//query parameter
	public static final String SELECT_OPR = "select";
	public static final String FILTER_OPR = "filter";
	public static final String PARTITION_OPR = "partition";
}
