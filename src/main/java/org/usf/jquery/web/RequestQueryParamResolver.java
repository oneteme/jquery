package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.COLUMN_DISTINCT;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.ContextManager.context;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.ContextManager.releaseContext;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.ResourceAccessException.accessDeniedException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.Validation;

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
public final class RequestQueryParamResolver {//spring connection bridge
	
	public RequestQueryBuilder requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		var t = currentTimeMillis();
		log.trace("parsing request...");
		parameterMap = new LinkedHashMap<>(parameterMap); //unmodifiable map
		if(!parameterMap.containsKey(COLUMN) && !parameterMap.containsKey(COLUMN_DISTINCT)) {
			parameterMap.put(COLUMN, ant.defaultColumns());
		}
		if(!isEmpty(ant.ignoreParameters())) {
			Stream.of(ant.ignoreParameters()).forEach(parameterMap::remove);
		}
		var ctx = ant.database().isEmpty() 
				? currentContext() 
				: context(ant.database());
		try {
			var req = ctx
					.lookupRegisteredView(requireLegalVariable(ant.view()))
					.orElseThrow(()-> noSuchResourceException(VIEW, ant.view()))
					.query(parameterMap); //may edit map
			log.trace("request parsed in {} ms", currentTimeMillis() - t);
			if(!ant.aggregationOnly() || req.isAggregation()) {
				return req;
			}
			throw accessDeniedException("non-aggregate query");
		}
		finally {
			releaseContext();
		}
	}
}
