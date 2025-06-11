package org.usf.jquery.web;

import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

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
		
		parameterMap.keySet().forEach(k-> 
		illegalArgumentIf(KEYWORDS.contains(k) && !contains(ant.mergeParameters(),k, (e,v)-> e.name().toLowerCase().equals(v)), ()-> k + " argument not allowed"));
		
		var modifiableMap = new LinkedHashMap<>(parameterMap); // modifiable map + preserve order
		appendParam(modifiableMap, COLUMN_PARAM, ant.column());
		appendParam(modifiableMap, DISTINCT_PARAM, ant.distinct()); // if allow override anno. distinct
		appendParam(modifiableMap, JOIN_PARAM, ant.join());
		appendParam(modifiableMap, ORDER_PARAM, ant.order());
		appendParam(modifiableMap, LIMIT_PARAM, ant.limit()); // if allow override anno. limit
		appendParam(modifiableMap, OFFSET_PARAM, ant.offset()); // if allow override anno. offset
		appendParams(modifiableMap, ant.filters());
		ignoreParams(modifiableMap, ant.ignoreParameters()); //order important !
		
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		return getRequestParser().parse(env, ant.view(), ant.variables(), modifiableMap);
	}

	void allowParams(Set<String> keywords, String[] allowedParams) {
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
	// ant(value) : column = id,contact
	// url(params) : column = id,country
	// result -> url : id,country,contact
	void appendParam(Map<String, String[]> params, String key, String[] value) {
		if (!isEmpty(value)) {
			params.compute(key, (k,v)-> isEmpty(v)
					? value // no URL param 
					: concat(stream(v), stream(value)).distinct().toArray(String[]::new));
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
	<T,U> boolean contains(T[] arr, U element, BiPredicate<T,U> pre) {
		for (T o : arr) {
			if(pre.test(o, element)) {
				return true;
			}
		}
		return false;
	}
	
	boolean checkForMerge(Map<String, String[]> params, String key, String[] allowedParams) {
		return Arrays.asList(allowedParams).contains(key) && params.get(key) != null;
	}
	
	boolean checkForMerge(Map<String, String[]> params, String key) {
		return params.get(key) != null;
	}
	
	public static void main(String[] args) {
		var x = "amine";
		var y = "AMINE".toLowerCase(); 
		System.out.println( x.equals(y));
	}
}
