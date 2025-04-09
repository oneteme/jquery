package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.ContextManager.context;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.ContextManager.releaseContext;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.COLUMN_DISTINCT;
import static org.usf.jquery.web.Parameters.VIEW;

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
		parameterMap = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
		if(!parameterMap.containsKey(COLUMN)) {
			parameterMap.computeIfAbsent(COLUMN_DISTINCT, k-> ant.defaultColumns());
		}
		if(!isEmpty(ant.ignoreParameters())) {
			for(var k : ant.ignoreParameters()) {
				parameterMap.remove(k);
			}
		}
		releaseContext(); //safety++
		var ctx = ant.database().isEmpty() 
				? currentContext()
				: context(ant.database());

		var req = ctx
				.lookupRegisteredView(ant.view())
				.orElseThrow(()-> noSuchResourceException(VIEW, ant.view()))
				.query(ctx.getCurrentQuery(), parameterMap); //may edit map
		log.trace("request parsed in {} ms", currentTimeMillis() - t);
		if(!ant.aggregationOnly() || req.isAggregation()) {
			return req;
		}
		throw new ResourceAccessException("non-aggregate query");
		//do not release context before query execute
	}
}
