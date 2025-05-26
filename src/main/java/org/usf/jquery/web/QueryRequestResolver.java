package org.usf.jquery.web;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.defaultEnvironment;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.JQuery.getRequestParser;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.Utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class QueryRequestResolver {//spring connection bridge
	
	private static final String COLUMN_DISTINCT = "column.distinct";
	
	public QueryComposer requestQuery(@NonNull QueryRequest ant, @NonNull Map<String, String[]> parameterMap) {
		var modifiableMap = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
		if(!isEmpty(ant.ignoreParameters())) {
			log.trace("ignoring parameters: {}", Arrays.toString(ant.ignoreParameters()));
			for(var k : ant.ignoreParameters()) {
				modifiableMap.remove(k);
			}
		}
		if(modifiableMap.containsKey(COLUMN_DISTINCT)) { //deprecated
			if(modifiableMap.containsKey(COLUMN_PARAM)) {
				throw new IllegalArgumentException(format("%s and %s are both set", COLUMN_DISTINCT, DISTINCT_PARAM));
			}
			modifiableMap.put(DISTINCT_PARAM, new String[] { "true" });
			modifiableMap.put(COLUMN_PARAM, modifiableMap.remove(COLUMN_DISTINCT));
			log.warn("column.distinct is deprecated, use distinct=true instead");
		}
		modifiableMap.computeIfAbsent(COLUMN_PARAM, k-> ant.defaultColumns());
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		var qry = getRequestParser().parse(env, ant.view(), ant.variables(), modifiableMap);
		if(!ant.aggregationOnly() || qry.isAggregation()) {
			return qry;
		}
		throw new ResourceAccessException("query is not aggregation");
	}
}
