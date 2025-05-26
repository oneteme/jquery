package org.usf.jquery.web;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;
import static org.usf.jquery.web.JQuery.defaultEnvironment;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.JQuery.getRequestParser;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.usf.jquery.core.QueryComposer;

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
public final class QueryRequestFilterResolver {// spring connection bridge

	private static final Set<String> KEYWORDS = Set.of(COLUMN_PARAM, DISTINCT_PARAM, JOIN_PARAM, OFFSET_PARAM, LIMIT_PARAM, ORDER_PARAM);

	public QueryComposer requestQueryCheck(@NonNull QueryRequestFilter ant, @NonNull Map<String, String[]> parameterMap) {
		var t = currentTimeMillis();
		log.trace("parsing request...");
		for (var s : KEYWORDS) {
			illegalArgumentIf(parameterMap.containsKey(s), () -> s + " argument not allowed");
		}
		var mutableMap = new LinkedHashMap<>(parameterMap); // modifiable map + preserve order
		appendParam(mutableMap, COLUMN_PARAM, ant.column());
		appendParam(mutableMap, DISTINCT_PARAM, ant.distinct());
		appendParam(mutableMap, JOIN_PARAM, ant.join());
		appendParam(mutableMap, OFFSET_PARAM, ant.order());
		appendParam(mutableMap, LIMIT_PARAM, ant.limit());
		appendParam(mutableMap, ORDER_PARAM, ant.offset());
		appendParams(parameterMap, ant.filters());
		if (!isEmpty(ant.ignoreParameters())) {
			for (var k : ant.ignoreParameters()) {
				mutableMap.remove(k);
			}
		}
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		var qry = getRequestParser().parse(env, ant.view(), ant.variables(), mutableMap);
		log.trace("request parsed in {} ms", currentTimeMillis() - t);
		return qry;
	}

	void appendParam(Map<String, String[]> params, String key, int value) {
		if (value > -1) {
			params.put(key, new String[] { valueOf(value) });
		}
	}

	void appendParam(Map<String, String[]> params, String key, boolean value) {
		if (value) {
			params.put(key, new String[] { valueOf(value) });
		}
	}

	void appendParam(Map<String, String[]> params, String key, String[] value) {
		if (!isEmpty(value)) {
			params.put(key, value);
		}
	}

	void appendParams(Map<String, String[]> params, String[] keys) {
		if (!isEmpty(keys)) {
			for(var filter : keys) {
				illegalArgumentIf(filter.chars().filter(v -> v == '=').count() > 1,
						()-> "incorrect filter format : '" + filter + "'");
				var arr = filter.split("=");
				illegalArgumentIf(arr[0].isEmpty(), ()-> "filter key cannot be null : '" + filter + "'");
				params.put(arr[0], arr.length > 1 ? new String[] { arr[1] } : null);
			}
		}
	}
}
