package org.usf.jquery.web;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;
import static org.usf.jquery.web.JQuery.defaultEnvironment;
import static org.usf.jquery.web.JQuery.exec;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.JQuery.getRequestParser;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.DISTINCT;
import static org.usf.jquery.web.Parameters.JOIN;
import static org.usf.jquery.web.Parameters.LIMIT;
import static org.usf.jquery.web.Parameters.OFFSET;
import static org.usf.jquery.web.Parameters.ORDER;

import java.util.Arrays;
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
public final class RequestParameterResolver2 {// spring connection bridge
	
	private static final Set<String> KEYWORDS = Set.of(COLUMN, DISTINCT, JOIN, OFFSET, LIMIT, ORDER);

	public QueryComposer requestQueryCheck(@NonNull RequestQueryParam2 ant, @NonNull Map<String, String[]> parameterMap) {
		var t = currentTimeMillis();
		log.trace("parsing request...");
		for(var s : KEYWORDS) {
			illegalArgumentIf(parameterMap.containsKey(s), ()-> s + " argument not allowed");
		}
		var mutableMap = new LinkedHashMap<>(parameterMap); // modifiable map + preserve order
		appendParam(mutableMap, COLUMN, ant.column());
		appendParam(mutableMap, DISTINCT, ant.distinct());
		appendParam(mutableMap, JOIN, ant.join());
		appendParam(mutableMap, ORDER, ant.order());
		appendParam(mutableMap, LIMIT, ant.limit());
		appendParam(mutableMap, OFFSET, ant.offset());
		if(!isEmpty(ant.filters())) {
			for (String filter : ant.filters()) {
				var splittedFilter = filter.split("=", 2);
				illegalArgumentIf(splittedFilter[0].length()<=0 || splittedFilter[0].contains("=") || (splittedFilter.length>1 && splittedFilter[1].contains("=")), ()-> "Incorrect filter format => " + filter);
				generateFilterField(mutableMap, splittedFilter);
			} 
		}
		if (!isEmpty(ant.ignoreParameters())) {
			for (var k : ant.ignoreParameters()) {
				mutableMap.remove(k);
			}
		}
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		return exec(env, e-> getRequestParser().parse(e, ant.view(), ant.variables(), mutableMap));
	}
	
	private void appendParam(Map<String, String[]> params, String key, int value) {
		if(value > -1) {
			params.put(key, new String[] {valueOf(value)});
		}
	}
	
	private void appendParam(Map<String, String[]> params, String key, boolean value) {
		if(value) {
			params.put(key, new String[] {valueOf(value)});
		}
	}

	private void appendParam(Map<String, String[]> params, String key, String[] value) {
		if(!isEmpty(value)) {
			params.put(key, value);
		}
	}
	private void generateFilterField(Map<String, String[]> params,String[] arr) {
		String filterValue = arr.length>1 && arr[1].length() > 0 ? arr[1] : "";
		params.put(arr[0],new String[] {filterValue});
	}
}
