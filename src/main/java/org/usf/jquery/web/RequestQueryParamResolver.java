package org.usf.jquery.web;

import static java.util.Collections.unmodifiableMap;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.RequestQueryBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQueryBuilder requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		parameterMap = new LinkedHashMap<>(parameterMap); //unmodifiable map
		if(!parameterMap.containsKey(COLUMN) && !parameterMap.containsKey(COLUMN_DISTINCT)) {
			parameterMap.put(COLUMN, ant.defaultColumns());
		}
		if(isPresent(ant.ignoreParameters())) {
			Stream.of(ant.ignoreParameters()).forEach(parameterMap::remove);
		}
		var req = context()
				.getTable(ant.name())
				.query(unmodifiableMap(parameterMap));
		if(ant.aggregationOnly() && !req.isAggregation()) {
			throw new IllegalDataAccessException("non aggregation query");
		}
		return req;
	}
}
