package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.defaultEnvironment;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.QueryComposer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class RequestParameterResolver {//spring connection bridge
	
	public QueryComposer requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		var t = currentTimeMillis();
		log.trace("parsing request...");
		var modifiableMap = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
		if(!isEmpty(ant.ignoreParameters())) {
			log.trace("ignoring parameters: {}", Arrays.toString(ant.ignoreParameters()));
			for(var k : ant.ignoreParameters()) {
				modifiableMap.remove(k);
			}
		}
		if(modifiableMap.containsKey("column.distinct")) { //deprecated
			log.warn("column.distinct is deprecated, use distinct=true instead");
			modifiableMap.put(DISTINCT_PARAM, new String[] { "true" });
			modifiableMap.put(COLUMN_PARAM, modifiableMap.remove("column.distinct"));
		}
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		var qry = env.parse(ant.view(), ant.variables(), modifiableMap);
		if(!ant.aggregationOnly() || qry.isAggregation()) {
			log.trace("request parsed in {} ms", currentTimeMillis() - t);
			return qry;
		}
		throw new ResourceAccessException("query is not aggregation");
	}
}
