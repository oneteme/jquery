package org.usf.jquery.web;

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
		return DatabaseScanner.get()
				.database()
				.getTable(ant.name())
				.query(ant, parameterMap);
	}
}
