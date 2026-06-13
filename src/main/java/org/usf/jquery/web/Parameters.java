package org.usf.jquery.web;

import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Parameters {

	//parameters
	public static final String VIEW_PARAM = "cte";
	@Deprecated(since = "5.0.0", forRemoval = true)
	public static final String COLUMN_PARAM = "column";
	public static final String FIELD_PARAM = "field";
	public static final String GROUP_PARAM = "group";
	public static final String DISTINCT_PARAM = "distinct";
	public static final String JOIN_PARAM = "join";
	public static final String ORDER_PARAM = "order";
	public static final String LIMIT_PARAM = "limit";
	public static final String OFFSET_PARAM = "offset";
	//query parameter
	public static final String SELECT_OPR = "select";
	@Deprecated(since = "5.0.0", forRemoval = true)
	public static final String FILTER_OPR = "filter";
	public static final String CRITERIA_OPR = "criteria";
	public static final String PARTITION_OPR = "partition";

	//useful for resource validation and to prevent reserved word usage in query parameters
	public static Set<String> reservedWords() {
		return Set.of(
				VIEW_PARAM, FIELD_PARAM, DISTINCT_PARAM, JOIN_PARAM, ORDER_PARAM, LIMIT_PARAM, OFFSET_PARAM,
				SELECT_OPR, FILTER_OPR, CRITERIA_OPR, PARTITION_OPR);
	}
	
}
