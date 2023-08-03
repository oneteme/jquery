package org.usf.jquery.web;

import static org.usf.jquery.web.JQueryContext.context;

import java.util.Map;

import org.usf.jquery.core.RequestQuery;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class RequestQueryParamResolver {
	
	public RequestQuery requestQuery(@NonNull RequestQueryParam ant, @NonNull Map<String, String[]> parameterMap) {
		return context()
				.getTable(ant.name())
				.query(ant, parameterMap);
	}
}
