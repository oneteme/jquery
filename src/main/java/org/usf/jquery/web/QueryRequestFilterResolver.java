package org.usf.jquery.web;

import static java.lang.String.valueOf;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.Utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class QueryRequestFilterResolver {// spring connection bridge
	
	private static final Set<String> KEYWORDS = Set.of(COLUMN_PARAM, DISTINCT_PARAM, JOIN_PARAM, OFFSET_PARAM, LIMIT_PARAM, ORDER_PARAM);

	public QueryComposer requestQueryCheck(@NonNull QueryRequestFilter ant, @NonNull Map<String, String[]> parameterMap) {
		
		var keywords = new HashSet<String>(KEYWORDS);
		allowParams(keywords, ant.allowParameters());
		parameterMap.keySet().forEach(k-> illegalArgumentIf(keywords.contains(k), ()-> k + " argument not allowed"));
		
		var modifiableMap = new LinkedHashMap<>(parameterMap); // modifiable map + preserve order
		appendParam(modifiableMap, COLUMN_PARAM, ant.column());
		appendParam(modifiableMap, DISTINCT_PARAM, ant.distinct()); // if allow override anno. distinct
		appendParam(modifiableMap, JOIN_PARAM, ant.join());
		appendParam(modifiableMap, ORDER_PARAM, ant.order());
		appendParam(modifiableMap, LIMIT_PARAM, ant.limit()); // if allow override anno. limit
		appendParam(modifiableMap, OFFSET_PARAM, ant.offset()); // if allow override anno. offset
		appendParams(modifiableMap, ant.filters());
		
		ignoreParams(modifiableMap, ant.ignoreParameters());
		
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		return getRequestParser().parse(env, ant.view(), ant.variables(), modifiableMap);
	}

	void allowParams(Set<String>keywords, String[] allowedParams) {
		if (!isEmpty(allowedParams)) {
			for (var k : allowedParams) {
				keywords.remove(k);
			}
		}
	}
	void ignoreParams(Map<String, String[]> params, String[] ignoredParams) {
		if (!isEmpty(ignoredParams)) {
			for (var k : ignoredParams) {
				params.remove(k);
			}
		}
	}
	
	void appendParam(Map<String, String[]> params, String key, int value) {
		if (value > -1) {
			params.putIfAbsent(key, new String[] { valueOf(value) });
		}
	}

	void appendParam(Map<String, String[]> params, String key, boolean value) {
		if (value) {
			params.putIfAbsent(key, new String[] { valueOf(value) });
		}
	}

	void appendParam(Map<String, String[]> params, String key, String[] value, String[] allowedParams) {
		if (!isEmpty(value)) {
			if(checkForMerge(params, key, allowedParams)) {
				var urlVal = params.get(key);
				for (String val : value) {
					if (!Arrays.asList(urlVal).contains(val)) {		
						Utils.append(urlVal, val);
					}
				}
				value = urlVal;
			}				
			params.putIfAbsent(key, value);
			
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
	boolean checkForMerge(Map<String, String[]> params, String key, String[] allowedParams) {
		return Arrays.asList(allowedParams).contains(key) && params.get(key) != null;
	}
	
	boolean checkForMerge(Map<String, String[]> params, String key) {
		return params.get(key) != null;
	}
}
