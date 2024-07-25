package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.DatabaseManager.currentDatabase;
import static org.usf.jquery.web.DatabaseManager.switchDatabase;

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
public final class RequestQueryParamResolver {
	
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
		var db = ant.database().isEmpty() ? currentDatabase() : switchDatabase(ant.database());
		var req = db.getConfig()
				.getTable(ant.view())
				.query(parameterMap); //may edit map
		if(ant.aggregationOnly() && !req.isAggregation()) {
			throw new IllegalDataAccessException("non aggregation query");
		}
        log.trace("request parsed in {} ms", currentTimeMillis() - t);
		return req;
	}
}
