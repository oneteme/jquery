package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.ContextManager.context;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.ContextManager.releaseContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.RequestQueryBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class RequestQueryParamResolver {//spring connection bridge
	
	public RequestQueryBuilder requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		var t = currentTimeMillis();
		log.trace("parsing request...");
		parameterMap = new LinkedHashMap<>(parameterMap); //unmodifiable map
		if(!parameterMap.containsKey(COLUMN) && !parameterMap.containsKey(COLUMN_DISTINCT)) {
			parameterMap.put(COLUMN, ant.defaultColumns());
		}
		if(isPresent(ant.ignoreParameters())) {
			Stream.of(ant.ignoreParameters()).forEach(parameterMap::remove);
		}
		var ctx = ant.database().isEmpty() 
				? currentContext() 
				: context(ant.database());
		try {
			var req = ctx
					.getTable(ant.view())
					.query(parameterMap); //may edit map
			if(!ant.aggregationOnly() || req.isAggregation()) {
		        log.trace("request parsed in {} ms", currentTimeMillis() - t);
				return req;
			}
			throw new IllegalDataAccessException("non aggregation query");
		}
		finally {
			releaseContext();
		}
	}
}
