package org.usf.jquery.web;

import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.usf.jquery.core.QueryComposer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class QueryRequestFilterResolver {// spring connection bridge
	
	private static final Set<String> KEYWORDS = stream(Keyword.values()).map(Keyword::getValue).collect(toSet());

	public QueryComposer requestQueryCheck(@NonNull QueryRequestFilter ant, @NonNull Map<String, String[]> parameterMap) {
		
		var illegalKeys = new HashSet<>(KEYWORDS);
		for(var k : ant.mergeParameters()) {
			illegalKeys.remove(k.getValue()); // override annotation clauses
		}
		parameterMap.keySet().forEach(k-> 
			illegalArgumentIf(illegalKeys.contains(k), ()-> k + " argument not allowed"));
		
		var modifiableMap = new LinkedHashMap<>(parameterMap);
		appendParam(modifiableMap, COLUMN_PARAM, ant.column());
		appendParam(modifiableMap, DISTINCT_PARAM, ant.distinct());
		appendParam(modifiableMap, JOIN_PARAM, ant.join());
		appendParam(modifiableMap, ORDER_PARAM, ant.order());
		appendParam(modifiableMap, LIMIT_PARAM, ant.limit());
		appendParam(modifiableMap, OFFSET_PARAM, ant.offset());
		appendParams(modifiableMap, ant.filters());
		ignoreParams(modifiableMap, ant.ignoreParameters()); //order important !
		
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		return getRequestParser().parse(env, ant.view(), ant.variables(), modifiableMap);
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

	void appendParam(Map<String, String[]> params, String key, String[] value) {
		if (!isEmpty(value)) {
			params.compute(key, (k,v)-> isEmpty(v)
					? value // no URL param 
					: concat(stream(value), stream(v)).distinct().toArray(String[]::new)); //merge same key values
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
	
	void ignoreParams(Map<String, String[]> params, String[] ignoredParams) {
		if (!isEmpty(ignoredParams)) {
			for(var k : ignoredParams) {
				illegalArgumentIf(KEYWORDS.contains(k), () -> "cannot ignore parameter: " + k);
				params.remove(k);
			}
		}
	}
}
