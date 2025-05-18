package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.currentContext;
import static org.usf.jquery.web.JQuery.getRequestParser;
import static org.usf.jquery.web.JQuery.lookupDatabase;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.DATABASE;
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
		var db = ant.database().isEmpty() 
				? lookupDatabase().orElseThrow(()-> new IllegalArgumentException("no default database"))
				: lookupDatabase(ant.database()).orElseThrow(()-> noSuchResourceException(DATABASE, ant.database()));

		var req = db.query(ant.view(), qry-> getRequestParser().parse(currentContext(), modifiableMap));
		
		log.trace("request parsed in {} ms", currentTimeMillis() - t);
		if(!ant.aggregationOnly() || req.isAggregation()) {
			return req;
		}
		throw new ResourceAccessException("non-aggregate query");
		//do not release context before query execute
	}
}
