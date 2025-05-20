package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.defaultEnvironment;
import static org.usf.jquery.web.JQuery.exec;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.JQuery.getRequestParser;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.DISTINCT;

import java.util.LinkedHashMap;
import java.util.Map;

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
public final class RequestParameterResolver {//spring connection bridge
	
	public QueryComposer requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		var t = currentTimeMillis();
		log.trace("parsing request...");
		var modifiableMap = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
		if(modifiableMap.containsKey("column.distinct")) { //deprecated
			log.warn("column.distinct is deprecated, use distinct=true instead");
			modifiableMap.put(DISTINCT, new String[] { "true" });
			modifiableMap.put(COLUMN, modifiableMap.remove("column.distinct"));
		}
		modifiableMap.computeIfAbsent(COLUMN, k-> ant.defaultColumns());
		if(!isEmpty(ant.ignoreParameters())) {
			for(var k : ant.ignoreParameters()) {
				modifiableMap.remove(k);
			}
		}
		var env = ant.database().isEmpty() ? defaultEnvironment() : getEnvironment(ant.database());
		var qry = exec(env, e-> getRequestParser().parse(e, ant.view(), modifiableMap));
		
		log.trace("request parsed in {} ms", currentTimeMillis() - t);
		if(!ant.aggregationOnly() || qry.isAggregation()) {
			return qry;
		}
		throw new ResourceAccessException("non-aggregate query");
		//do not release context before query execute
	}
}
